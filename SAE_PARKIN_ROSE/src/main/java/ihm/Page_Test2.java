package ihm;

import javax.swing.*;
import java.awt.*;

public class Page_Test2 extends JFrame {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Page_Test2(String email, String nom, String prenom) {
        initializeUI();
    }
    
    private void initializeUI() {
        this.setTitle("TEST DE PAGE GARER EN VOIRIE");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400, 200);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BorderLayout());
        
        // Juste le texte "test" centré
        JLabel lblTest = new JLabel("test", SwingConstants.CENTER);
        lblTest.setFont(new Font("Arial", Font.BOLD, 36));
        lblTest.setForeground(Color.BLACK);
        
        panel.add(lblTest, BorderLayout.CENTER);
        this.setContentPane(panel);
    }
}
