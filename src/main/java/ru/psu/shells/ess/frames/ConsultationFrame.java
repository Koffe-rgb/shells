package ru.psu.shells.ess.frames;

import ru.psu.shells.ess.components.InferenceEngine;
import ru.psu.shells.ess.components.WaitForAnswerListener;
import ru.psu.shells.ess.model.entity.DomainValue;
import ru.psu.shells.ess.model.entity.Variable;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsultationFrame extends JDialog {
    private JTextPane txtpnlQuestion;
    private JPanel rPanel;
    private JComboBox<DomainValue> cbxAnswer;
    private JButton btnOk;

    private final DefaultComboBoxModel<DomainValue> cbxAnswerModel = new DefaultComboBoxModel<>();

    private final WaitForAnswerListener engine;
    private DomainValue answer;

    private final ExecutorService poolExecutor = Executors.newSingleThreadExecutor();

    public ConsultationFrame(JFrame owner, Variable goal) {
        super(owner);
        this.setContentPane(rPanel);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        engine = new InferenceEngine(goal);

        ((InferenceEngine) engine).setConsultationFrame(ConsultationFrame.this);

        rootPane.setDefaultButton(btnOk);

        cbxAnswer.setModel(cbxAnswerModel);
        btnOk.addActionListener(btnOkSendAnswerAL);

        StyledDocument doc = txtpnlQuestion.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        poolExecutor.execute((Runnable) engine);

        this.setTitle("Режим консультации");
        this.setSize(400, 250);
        this.setModal(true);
        this.setLocationRelativeTo(owner);
        this.setResizable(false);
        this.setVisible(true);
    }

    public void setQuestion(Variable variable) {
        txtpnlQuestion.setText("\n\n\n" + variable.getQuestion());
        cbxAnswerModel.removeAllElements();
        variable.getDomain().getValues().forEach(cbxAnswerModel::addElement);
    }

    private final ActionListener btnOkSendAnswerAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            DomainValue selectedAnswer = (DomainValue) cbxAnswerModel.getSelectedItem();
            engine.sendUserAnswer(selectedAnswer);
        }
    };

    private final ActionListener btnOkExplainAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            new ConsultationResultFrame(null, engine.getWorkingMemory(), answer) ;
        }
    };

    public void setResult(DomainValue answer) {
        this.answer = answer;

        if (answer != null)
            txtpnlQuestion.setText("\n\n\nВаш ответ : " + answer.getValue());
        else
            txtpnlQuestion.setText("\n\n\nВаш ответ : НЕИЗВЕСТНО" );

        cbxAnswerModel.removeAllElements();
        cbxAnswer.setEnabled(false);

        btnOk.removeActionListener(btnOkSendAnswerAL);
        btnOk.addActionListener(e -> dispose());
        btnOk.addActionListener(btnOkExplainAL);
        btnOk.setText("Закрыть");

        close();
    }

    public void close() {
        if (!poolExecutor.isShutdown())
            poolExecutor.shutdown();
    }
}


