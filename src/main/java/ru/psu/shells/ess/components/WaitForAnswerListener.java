package ru.psu.shells.ess.components;

import ru.psu.shells.ess.model.entity.DomainValue;

public interface WaitForAnswerListener {
    void sendUserAnswer(DomainValue answer);
    WorkingMemory getWorkingMemory();
}
