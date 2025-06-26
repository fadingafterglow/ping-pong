package ua.edu.ukma.cs.utils;

import javax.swing.*;
import java.awt.*;

public class DialogUtils {

    public static void successDialog(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void errorDialog(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static String inputDialog(Component parent, String message) {
        return JOptionPane.showInputDialog(parent, message, "Input", JOptionPane.QUESTION_MESSAGE);
    }
}
