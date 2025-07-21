import javax.swing.*;
import java.awt.event.ActionEvent;

public class LoginMainFrame extends javax.swing.JFrame {

    public LoginMainFrame() {
        initComponents();
    }

    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        Usernametxt = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        Passwordtxt = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Login - Warehouse Manager MW");
        getContentPane().setLayout(null); // using absolute layout

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLabel1.setText("Welcome");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(270, 70, 100, 30);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel2.setText("Username");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(160, 130, 100, 25);

        getContentPane().add(Usernametxt);
        Usernametxt.setBounds(160, 160, 289, 30);

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel3.setText("Password");
        getContentPane().add(jLabel3);
        jLabel3.setBounds(160, 200, 100, 25);

        getContentPane().add(Passwordtxt);
        Passwordtxt.setBounds(160, 230, 289, 30);

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 14));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/sign-in.png")));
        jButton1.setText("Login");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        getContentPane().add(jButton1);
        jButton1.setBounds(160, 280, 289, 45);

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 14));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/door.png")));
        jButton2.setText("EXIT");
        jButton2.addActionListener(e -> System.exit(0));
        getContentPane().add(jButton2);
        jButton2.setBounds(160, 340, 289, 45);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/background.jpg")));
        jLabel4.setBounds(0, 0, 640, 480);
        getContentPane().add(jLabel4);

        setSize(640, 480);
        setLocationRelativeTo(null); // center the frame
    }

    private void jButton1ActionPerformed(ActionEvent evt) {
        String username = Usernametxt.getText();
        String password = new String(Passwordtxt.getPassword());

        if (username.equals("admin") && password.equals("admin")) {
            JOptionPane.showMessageDialog(this, "Login successful!");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        java.awt.EventQueue.invokeLater(() -> new LoginMainFrame().setVisible(true));
    }

    // Components
    private javax.swing.JPasswordField Passwordtxt;
    private javax.swing.JTextField Usernametxt;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
}
