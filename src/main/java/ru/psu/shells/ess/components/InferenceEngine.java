package ru.psu.shells.ess.components;

import ru.psu.shells.ess.frames.ConsultationFrame;
import ru.psu.shells.ess.model.entity.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// МЛВ
public class InferenceEngine implements WaitForAnswerListener, Runnable {
    private ConsultationFrame consultationFrame;    // ссылка на окно консультации
    private DomainValue answerFromUser;             // полученный ответ пользователя
    private final Object lock = new Object();       // lock, чтобы поток МЛВ ждал, пока пользователь не ответит
    private final WorkingMemory workingMemory;      // рабочая память

    private final Variable goal;    // глобальная цель

    public InferenceEngine(Variable goal) {
        this.goal = goal;
        workingMemory = new WorkingMemory(goal);
    }

    public void setConsultationFrame(ConsultationFrame consultationFrame) {
        this.consultationFrame = consultationFrame;
    }

    private DomainValue startConsultation(Variable goal) {
        // получаем из БЗ все правила, загружаем их в рабочую память как несработавшие правила
        Storage.getRules().forEach(rule -> workingMemory.getUntriggeredRules().add(rule));
        return find(goal);
    }

    // основной метод
    private DomainValue find(Variable variable) {
        // из уже известных фактов выбираем тот, у которого переменная совпадает с искомой
        Optional<Fact> factOptional = workingMemory.getKnownFacts().stream()
                .filter(f -> f.getVariable().equals(variable))
                .findFirst();
        // если факт уже был найден, прекращаем эту итерацию поиска
        if (factOptional.isPresent()) {
            return factOptional.get().getValue();
        }

        // запрашиваем значение у пользователя, если можно
        if (variable.getType() == VarType.REQUESTED) {
            return synchronizedAskQuestion(variable);
        }
        // фильтруем несработавшие правила, берем те, у которых переменная заключения совпадает с искомой
        List<Rule> untriggeredRules = workingMemory.getUntriggeredRules()
                .stream()
                .filter(r -> r.getConclusion().getVariable().equals(variable))
                .collect(Collectors.toList());
        // для всех найденных выше правил, пытаемся доказать их
        for (Rule rule : untriggeredRules) {
            Fact fact = proveRule(rule);
            // если доказали правило, сохраняем заключение, прекращаем поиск на это итерации
            if (fact != null) {
                workingMemory.getKnownFacts().add(fact);
                return fact.getValue();
            }
        }
        // если не удалось, и тип подходящий - запрашиваем
        if (variable.getType() == VarType.INFER_REQUESTED) {
            return synchronizedAskQuestion(variable);
        }

        // не удалось доказать
        return null;
    }

    private DomainValue synchronizedAskQuestion(Variable variable) {
        synchronized (lock) {   // делаем синхронизацию по локу
            askQuestion(variable); // спрашиваем, у пользователя в окне консультации меняется вопрос и ответы
            try {
                lock.wait(); // лочим этот поток и ждем
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // лок снимается в sendUserAnswer, методе интерфейса

        // запоминаем ответ юзера и возвращаем ответ в МЛВ
        Fact newFact = new Fact(variable, answerFromUser);
        workingMemory.getKnownFacts().add(newFact);
        return answerFromUser;
    }

    // доказательство правила
    private Fact proveRule(Rule rule) {
        // убираем из несработавших
        workingMemory.getUntriggeredRules().remove(rule);
        // для всех посылок
        for (Fact curFact : rule.getCondition()) {
            // берем факт, у которого переменная совпадает с переменной посылки
            Optional<Fact> fact = workingMemory.getKnownFacts()
                    .stream()
                    .filter(f -> f.getVariable().equals(curFact.getVariable()))
                    .findFirst();
            DomainValue domainValue = fact.isPresent() ?  // если переменная означена
                    fact.get().getValue() :               // берем значение, иначе
                    find(curFact.getVariable());          // ищем на очередной итерации значении переменной
            if (!curFact.getValue().equals(domainValue)) {
                return null;    // говорим, что правило не удалось доказать, если значения не равны
            }
        }

        Fact conclusion = rule.getConclusion(); // заключение
        workingMemory.getInferenceRulesMap().put(conclusion.getVariable(), rule); // запоминанием, к какой переменной относится доказанное правило

        return conclusion;
    }

    private void askQuestion(Variable variable) {
        consultationFrame.setQuestion(variable);
    }

    @Override
    public void sendUserAnswer(DomainValue answer) {
        synchronized (lock) {
            this.answerFromUser = answer;   // получаем ответ пользователя
            lock.notify();                  // снимаем лок
        }
    }

    // метод интерфейса Runnable - когда выделяем под этот класс поток, то именно он стартует
    @Override
    public void run() {
        DomainValue answer = startConsultation(this.goal);
        consultationFrame.setResult(answer);
    }

    public WorkingMemory getWorkingMemory() {
        return workingMemory;
    }
}
