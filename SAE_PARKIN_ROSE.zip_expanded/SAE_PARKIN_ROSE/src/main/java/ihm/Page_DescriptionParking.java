package ihm;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Color;
import javax.swing.JTextArea;
import javax.swing.JButton;

public class Page_DescriptionParking extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Page_DescriptionParking frame = new Page_DescriptionParking();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Page_DescriptionParking() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel titreParking = new JLabel("Nom parking");
		titreParking.setBounds(31, 11, 213, 36);
		titreParking.setFont(new Font("Arial", Font.BOLD, 25));
		contentPane.add(titreParking);
		
		JLabel adresseParking = new JLabel("Adresse du parking");
		adresseParking.setForeground(new Color(0, 0, 255));
		adresseParking.setFont(new Font("Arial", Font.PLAIN, 8));
		adresseParking.setBounds(31, 42, 95, 14);
		contentPane.add(adresseParking);
		
		JTextArea informationsParking = new JTextArea();
		informationsParking.setColumns(1);
		informationsParking.setText("blablabla");
		informationsParking.setEditable(false);
		informationsParking.setBounds(27, 83, 388, 125);
		contentPane.add(informationsParking);
		
		JLabel titreInfos = new JLabel("Informations");
		titreInfos.setFont(new Font("Arial", Font.BOLD, 14));
		titreInfos.setBounds(31, 58, 95, 14);
		contentPane.add(titreInfos);
		
		JButton boutonRéserver = new JButton("Réserver une place");
		boutonRéserver.setBounds(148, 219, 143, 34);
		contentPane.add(boutonRéserver);
	}
}
