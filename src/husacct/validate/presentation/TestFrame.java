/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package husacct.validate.presentation;

import husacct.validate.ValidateServiceImpl;
import husacct.validate.task.report.UnknownStorageTypeException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import org.w3c.dom.DOMException;

import com.itextpdf.text.DocumentException;

/**
 *
 * @author Jorik
 */
public class TestFrame extends javax.swing.JFrame {

	private static final long serialVersionUID = -2212704611368658020L;

	private ValidateServiceImpl impl;

	/**
	 * Creates new form TestFrame
	 */
	public TestFrame() {

		impl = new ValidateServiceImpl();
		impl.checkConformance();
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		ggt = new javax.swing.JDesktopPane();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setState(MAXIMIZED_BOTH);

		jButton1.setText("BrowseViolations");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		jButton2.setText("Configuration");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		jButton3.setText("Export PDF");
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton3ActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(ggt)
						.addGap(6, 6, 6))
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
								.addContainerGap(619, Short.MAX_VALUE)
								.addComponent(jButton3)
								.addGap(45, 45, 45)
								.addComponent(jButton1)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jButton2)
								.addGap(16, 16, 16))
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(ggt, javax.swing.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
						.addGap(9, 9, 9)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButton2)
								.addComponent(jButton1)
								.addComponent(jButton3))
								.addGap(3, 3, 3))
				);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed


		JInternalFrame bv = impl.getBrowseViolationsGUI();

		bv.setBounds(0, 0, 800, 600);
		this.ggt.add(bv);
		bv.moveToFront();
	}//GEN-LAST:event_jButton1ActionPerformed

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

		JInternalFrame cgui = impl.getConfigurationGUI();

		cgui.setBounds(10, 10, 800, 600);
		this.ggt.add(cgui);
		cgui.moveToFront();
	}//GEN-LAST:event_jButton2ActionPerformed

	private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
		try {
			impl.exportViolations("TestReport.html", "html", "C:\\reports");
			JOptionPane.showMessageDialog(rootPane, "Created at C:\\reports");
		} catch (DOMException ex) {
			Logger.getLogger(TestFrame.class.getName()).log(Level.SEVERE, null,
<<<<<<< HEAD
					ex);
		} catch (UnknownStorageTypeException ex) {
			Logger.getLogger(TestFrame.class.getName()).log(Level.SEVERE, null,
					ex);
		} catch (IOException ex) {
			Logger.getLogger(TestFrame.class.getName()).log(Level.SEVERE, null,
					ex);
		} catch (URISyntaxException ex) {
			Logger.getLogger(TestFrame.class.getName()).log(Level.SEVERE, null,
					ex);
		} catch (DocumentException ex) {
			Logger.getLogger(TestFrame.class.getName()).log(Level.SEVERE, null,
					ex);
=======
															ex);		
>>>>>>> 93be94728f800ccaf7972e05e10db14036d599c6
		}

	}//GEN-LAST:event_jButton3ActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/*
		 * Set the Nimbus look and feel
		 */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
		 * default look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Windows".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(TestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(TestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(TestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(TestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/*
		 * Create and display the form
		 */
		java.awt.EventQueue.invokeLater(new Runnable() {

			public void run() {
				new TestFrame().setVisible(true);
			}
		});
	}
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JDesktopPane ggt;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	// End of variables declaration//GEN-END:variables
}
