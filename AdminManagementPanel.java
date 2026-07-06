package SpaceResourceBooking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminManagementPanel extends JPanel {

    /**
	 *	
	 */
	private static final long serialVersionUID = 1L;
	private final Management managementDAO = new Management();
    private static final Color PRIMARY_NAVY = new Color(15, 44, 89);
    private static final Color BACKGROUND_GRAY = new Color(195, 199, 206);

    public AdminManagementPanel() {
        setLayout(new GridLayout(0, 1, 20, 20)); // Grid layout for vertically stacked buttons/forms
        setBorder(new EmptyBorder(50, 100, 50, 100));
        setBackground(BACKGROUND_GRAY);

        // Header
        JLabel header = new JLabel("Admin Management Tools", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 24));
        header.setForeground(PRIMARY_NAVY);
        add(header);
        
        // 1. Cleanup Maintenance Logs Button
        JButton cleanupBtn = createActionButton("Cleanup Maintenance Logs (Older than 6 Months)");
        cleanupBtn.addActionListener(e -> deleteOldMaintenanceLogs());
        add(cleanupBtn);

        // 2. Update Resource Price Form
        add(createUpdatePriceForm());
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_NAVY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        return button;
    }

    private JPanel createUpdatePriceForm() {
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Update Resource Hourly Rate"));
        formPanel.setBackground(BACKGROUND_GRAY);

        JTextField idField = new JTextField(10);
        JTextField rateField = new JTextField(10);
        JButton updateBtn = createActionButton("Update Rate");
        
        updateBtn.addActionListener(e -> {
            try {
                int resourceId = Integer.parseInt(idField.getText());
                double newRate = Double.parseDouble(rateField.getText());
                
                if (managementDAO.updateResourceRate(resourceId, newRate)) {
                    JOptionPane.showMessageDialog(this, "Resource ID " + resourceId + " rate updated to $" + String.format("%.2f", newRate), "Success", JOptionPane.INFORMATION_MESSAGE);
                    idField.setText("");
                    rateField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed. Check ID or database connection.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for ID and Rate.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formPanel.add(new JLabel("Resource ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("New Hourly Rate:"));
        formPanel.add(rateField);
        formPanel.add(new JLabel("")); // Spacer
        formPanel.add(updateBtn);
        
        return formPanel;
    }

    private void deleteOldMaintenanceLogs() {
        int count;
        try {
            count = managementDAO.deleteOldMaintenance();
            if (count >= 0) {
                 JOptionPane.showMessageDialog(this, count + " old maintenance logs have been cleaned up.", "Cleanup Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(this, "Cleanup failed. Check logs for database error.", "Cleanup Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error during cleanup: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}