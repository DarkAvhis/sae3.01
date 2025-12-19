package vue;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog simple pour éditer une liaison (multiplicités, rôles et offsets).
 */
public class EditeurLiaisonDialog extends JDialog 
{

	private final JTextField tfMultOrig = new JTextField(10);
	private final JTextField tfMultDest = new JTextField(10);
	private final JTextField tfRoleOrig = new JTextField(12);
	private final JTextField tfRoleDest = new JTextField(12);

	private JButton btnOK    ;
	private JButton btnCancel;

	public EditeurLiaisonDialog(Frame owner, LiaisonVue liaison) 
	{
		super(owner, "Éditer la liaison", true);

		tfMultOrig.setText(liaison.getMultipliciteOrig());
		tfMultDest.setText(liaison.getMultipliciteDest());
		tfRoleOrig.setText(liaison.getRoleOrig());
		tfRoleDest.setText(liaison.getRoleDest());

		JPanel panelDisposition = new JPanel(new GridLayout(0, 2, 6, 6));
		panelDisposition.add(new JLabel("Multiplicité origine:"    ));
		panelDisposition.add(tfMultOrig);
		panelDisposition.add(new JLabel("Multiplicité destination:"));
		panelDisposition.add(tfMultDest);
		panelDisposition.add(new JLabel("Rôle origine:"            ));
		panelDisposition.add(tfRoleOrig);
		panelDisposition.add(new JLabel("Rôle destination:"        ));
		panelDisposition.add(tfRoleDest);

		this.btnOK = new JButton("OK");
		this.btnOK.addActionListener((ActionEvent e) -> 
		{
			liaison.setRoleOrig(tfRoleOrig.getText()        );
			liaison.setRoleDest(tfRoleDest.getText()        );
			// Multiplicités
			liaison.setMultipliciteOrig(tfMultOrig.getText());
			liaison.setMultipliciteDest(tfMultDest.getText());
			dispose();
		});

		this.btnCancel = new JButton("Annuler");
		this.btnCancel.addActionListener((ActionEvent e) -> dispose());

		JPanel panelAction = new JPanel();

		panelAction.add(this.btnOK);
		panelAction.add(this.btnCancel);

		getContentPane().add(panelDisposition, BorderLayout.CENTER);
		getContentPane().add(panelAction, BorderLayout.SOUTH);
		
		pack();
		setLocationRelativeTo(owner);
	}
}
