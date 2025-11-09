package com.mycompany.quanlythuvien;

import javax.swing.UIManager;

import com.mycompany.quanlythuvien.view.JFrame_Login;

/**
 *
 * @author Tien
 */
public class Main {

    public static void main(String[] args) {
        try {
            for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrame_Login().setVisible(true);
            }
        });
    }
}
