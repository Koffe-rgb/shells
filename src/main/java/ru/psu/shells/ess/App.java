package ru.psu.shells.ess;

import ru.psu.shells.ess.frames.MainFrame;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class App {
    public static void main(String[] args) {
        // установка стиля отображения окон на обычный Windows'кий
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        // обработка закрытия окна, когда есть несохраненные изменения
        MainFrame main = new MainFrame();
        main.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                main.checkSave(true);
            }
        });
    }
}
