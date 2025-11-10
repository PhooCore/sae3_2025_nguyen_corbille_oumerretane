package ihm;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Page_Accueil extends JPanel {
	private JTextField txtRechercher;

	/**
	 * Create the panel.
	 */
	public Page_Accueil() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel mainPanel = new JPanel();
		add(mainPanel);
		mainPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel titlePanel = new JPanel();
		mainPanel.add(titlePanel, BorderLayout.NORTH);
		titlePanel.setLayout(new GridLayout(0, 7, 0, 0));
		
		JLabel iconLabel = new JLabel("icon");
		titlePanel.add(iconLabel);
		
		JLabel ttlLabel = new JLabel("PARKIN'ROSE");
		ttlLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 10));
		titlePanel.add(ttlLabel);
		
		Component rigidArea_4 = Box.createRigidArea(new Dimension(0, 30));
		titlePanel.add(rigidArea_4);
		
		Component rigidArea_3 = Box.createRigidArea(new Dimension(0, 30));
		titlePanel.add(rigidArea_3);
		
		Component rigidArea = Box.createRigidArea(new Dimension(0, 30));
		titlePanel.add(rigidArea);
		
		JButton favBtn = new JButton("Favoris");
		titlePanel.add(favBtn);
		
		JButton compteBtn = new JButton("Compte");
		titlePanel.add(compteBtn);
		
		JPanel bodyPanel = new JPanel();
		mainPanel.add(bodyPanel, BorderLayout.CENTER);
		bodyPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel conteneurCartePanel = new JPanel();
		bodyPanel.add(conteneurCartePanel, BorderLayout.CENTER);
		
		Component rigidArea_5 = Box.createRigidArea(new Dimension(0, 30));
		conteneurCartePanel.add(rigidArea_5);
		
		JPanel panel = new JPanel();
		bodyPanel.add(panel, BorderLayout.NORTH);
		panel.setLayout(new GridLayout(0, 5, 0, 0));
		
		txtRechercher = new JTextField();
		txtRechercher.setText("Rechercher");
		txtRechercher.setColumns(10);
		panel.add(txtRechercher);
		
		JButton rechercheBtn = new JButton("icon Rechercher");
		panel.add(rechercheBtn);
		
		Component rigidArea_1 = Box.createRigidArea(new Dimension(0, 30));
		panel.add(rigidArea_1);
		
		Component rigidArea_2 = Box.createRigidArea(new Dimension(0, 30));
		panel.add(rigidArea_2);
		
		JComboBox fltCBox = new JComboBox();
		fltCBox.setModel(new DefaultComboBoxModel(new String[] {"tous les parking", "un filtre", "un autre", "encore", "c long"}));
		panel.add(fltCBox);
		
		JPanel btnPanel = new JPanel();
		mainPanel.add(btnPanel, BorderLayout.SOUTH);
		
		JButton listeBtn = new JButton("Voir liste ");
		btnPanel.add(listeBtn);

	}

}
