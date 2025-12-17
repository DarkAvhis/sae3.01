package vue;

import java.awt.BorderLayout;
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
public class EditeurLiaisonDialog extends JDialog {
	private final LiaisonVue liaison;

	private final JTextField tfMultOrig = new JTextField(10);
	private final JTextField tfMultDest = new JTextField(10);
	private final JTextField tfRoleOrig = new JTextField(12);
	private final JTextField tfRoleDest = new JTextField(12);
	private final JTextField tfOrigAlong = new JTextField(4);
	private final JTextField tfOrigPerp = new JTextField(4);
	private final JTextField tfDestAlong = new JTextField(4);
	private final JTextField tfDestPerp = new JTextField(4);

	public EditeurLiaisonDialog(java.awt.Frame owner, LiaisonVue liaison) {
		super(owner, "Éditer la liaison", true);
		this.liaison = liaison;

		tfMultOrig.setText(liaison.getMultipliciteOrig());
		tfMultDest.setText(liaison.getMultipliciteDest());
		tfRoleOrig.setText(liaison.getRoleOrig());
		tfRoleDest.setText(liaison.getRoleDest());
		tfOrigAlong.setText(String.valueOf(liaison.getRoleOrigOffsetAlong()));
		tfOrigPerp.setText(String.valueOf(liaison.getRoleOrigOffsetPerp()));
		tfDestAlong.setText(String.valueOf(liaison.getRoleDestOffsetAlong()));
		tfDestPerp.setText(String.valueOf(liaison.getRoleDestOffsetPerp()));

		JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
		form.add(new JLabel("Multiplicité origine:"));
		form.add(tfMultOrig);
		form.add(new JLabel("Multiplicité destination:"));
		form.add(tfMultDest);
		form.add(new JLabel("Rôle origine:"));
		form.add(tfRoleOrig);
		form.add(new JLabel("Rôle destination:"));
		form.add(tfRoleDest);
		form.add(new JLabel("Orig offset (le long):"));
		form.add(tfOrigAlong);
		form.add(new JLabel("Orig offset (perp):"));
		form.add(tfOrigPerp);
		form.add(new JLabel("Dest offset (le q long):"));
		form.add(tfDestAlong);
		form.add(new JLabel("Dest offset (perp):"));
		form.add(tfDestPerp);

		JButton btnOK = new JButton("OK");
		btnOK.addActionListener((ActionEvent e) -> {
			liaison.setRoleOrig(tfRoleOrig.getText());
			liaison.setRoleDest(tfRoleDest.getText());
			liaison.setRoleOrigOffsetAlong(parseIntSafe(tfOrigAlong.getText(), liaison.getRoleOrigOffsetAlong()));
			liaison.setRoleOrigOffsetPerp(parseIntSafe(tfOrigPerp.getText(), liaison.getRoleOrigOffsetPerp()));
			liaison.setRoleDestOffsetAlong(parseIntSafe(tfDestAlong.getText(), liaison.getRoleDestOffsetAlong()));
			liaison.setRoleDestOffsetPerp(parseIntSafe(tfDestPerp.getText(), liaison.getRoleDestOffsetPerp()));
			// Multiplicités
			liaison.setMultipliciteOrig(tfMultOrig.getText());
			liaison.setMultipliciteDest(tfMultDest.getText());
			dispose();
		});

		JButton btnCancel = new JButton("Annuler");
		btnCancel.addActionListener((ActionEvent e) -> dispose());

		JPanel actions = new JPanel();
		actions.add(btnOK);
		actions.add(btnCancel);

		getContentPane().add(form, BorderLayout.CENTER);
		getContentPane().add(actions, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(owner);
	}

	private int parseIntSafe(String s, int def) {
		try {
			return Integer.parseInt(s.trim());
		} catch (Exception ex) {
			return def;
		}
	}
}
