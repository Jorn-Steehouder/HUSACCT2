package husacct.analyse.presentation.reconstruct;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import husacct.ServiceProvider;
import husacct.analyse.task.AnalyseTaskControl;
import husacct.analyse.task.reconstruct.ReconstructArchitecture;
import husacct.common.dto.ModuleDTO;
import husacct.common.dto.ReconstructArchitectureDTO;
import husacct.common.help.presentation.HelpableJPanel;
import husacct.common.locale.ILocaleService;
import husacct.control.task.MainController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;

public class ReconstructJPanel extends HelpableJPanel implements ActionListener{
	private final Logger logger = Logger.getLogger(ReconstructJPanel.class);
	private static final int approachesThresholdCollumn = 1;
	private static final int approachNameCollumn = 0;
	private static final long serialVersionUID = 1L;
	private ApproachesTableJPanel approachesTableJPanel;
	private AnalyseTaskControl analyseTaskControl;
	private JButton applyButton, reverseButton, clearButton, testButton;
	private JPanel panel;
	private ILocaleService localService;
	
	/**
	 * Create the panel.
	 */
	public ReconstructJPanel(AnalyseTaskControl atc) {
		super();
		analyseTaskControl = atc;
		localService = ServiceProvider.getInstance().getLocaleService();
		initUI();
	}
	
	public JPanel createApproachesTableJPanel(){
		approachesTableJPanel = new ApproachesTableJPanel();
		return approachesTableJPanel;
	}
	
	public JPanel createButtons(){
		return null;
	}
	
	public final void initUI(){
		setLayout(new GridLayout(0, 1, 0, 0));
		this.add(createApproachesTableJPanel(), BorderLayout.CENTER);
		
		panel = new JPanel();
		approachesTableJPanel.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout());
		
		String applyTranslation = localService.getTranslatedString("Apply");
		applyButton = new JButton(applyTranslation);
		panel.add(applyButton);
		applyButton.setPreferredSize(new Dimension(100, 40));
		applyButton.addActionListener(this);

		String reverseTranslation = localService.getTranslatedString("Reverse");
		reverseButton = new JButton(reverseTranslation);
		panel.add(reverseButton);
		reverseButton.setPreferredSize(new Dimension(reverseButton.getPreferredSize().width, 40));
		reverseButton.addActionListener(this);
		
		String clearAllTranslation = localService.getTranslatedString("ClearAll");
		clearButton = new JButton(clearAllTranslation);
		panel.add(clearButton);
		clearButton.setPreferredSize(new Dimension(100, 40));
		clearButton.addActionListener(this);
		
		testButton = new JButton("Test");
		panel.add(testButton);
		testButton.setPreferredSize(new Dimension(100, 40));
		testButton.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent action) {
		if (action.getSource() == applyButton) {
			JTable approachesTable = approachesTableJPanel.approachesTable;
			ButtonGroup radioButtonsRelationType = approachesTableJPanel.RadioButtonsRelationType;
			ButtonGroup buttonGroupTwo = approachesTableJPanel.radioButtonGroupTwo;

			int selectedRow = approachesTable.getSelectedRow();
			if (selectedRow >= 0){
				String approach = (String) approachesTable.getModel().getValueAt(selectedRow, 0);
				int threshold = Integer.parseInt(approachesTable.getValueAt(selectedRow, approachesThresholdCollumn).toString());
				String relationType = (radioButtonsRelationType.getSelection() != null)	? radioButtonsRelationType.getSelection().getActionCommand() : "";

				ReconstructArchitectureDTO dto = new ReconstructArchitectureDTO();
				ModuleDTO selectedModule = getSelectedModule();
				dto.setSelectedModule(selectedModule);
				dto.setApproach(approach);
				dto.setThreshold(threshold);
				dto.setRelationType(relationType);		
				
				analyseTaskControl.reconstructArchitecture_Execute(dto);
				ServiceProvider.getInstance().getDefineService().getSarService().updateModulePanel();
			}
			else{
				logger.warn("No Approache selected");
			}

		}
		if (action.getSource() == reverseButton) {
			JTable approachesTable = approachesTableJPanel.approachesTable;
			approachesTable.clearSelection();
			analyseTaskControl.reconstructArchitecture_Reverse();
			ServiceProvider.getInstance().getDefineService().getSarService().updateModulePanel();
		}
		if (action.getSource() == clearButton) {
			JTable approachesTable = approachesTableJPanel.approachesTable;
			approachesTable.clearSelection();
			analyseTaskControl.reconstructArchitecture_ClearAll();
			ServiceProvider.getInstance().getDefineService().getSarService().updateModulePanel();
		}
		if (action.getSource() == testButton){
			JTable approachesTable = approachesTableJPanel.approachesTable;
			ButtonGroup radioButtonsRelationType = approachesTableJPanel.RadioButtonsRelationType;
			ButtonGroup buttonGroupTwo = approachesTableJPanel.radioButtonGroupTwo;

			int selectedRow = approachesTable.getSelectedRow();
			if (selectedRow >= 0){
				String approach = (String) approachesTable.getModel().getValueAt(selectedRow, 0);
				int threshold = Integer.parseInt(approachesTable.getValueAt(selectedRow, approachesThresholdCollumn).toString());
				String relationType = (radioButtonsRelationType.getSelection() != null)	? radioButtonsRelationType.getSelection().getActionCommand() : "";

				ReconstructArchitectureDTO dto = new ReconstructArchitectureDTO();
				ModuleDTO selectedModule = getSelectedModule();
				dto.setSelectedModule(selectedModule);
				dto.setApproach(approach);
				dto.setThreshold(threshold);
				dto.setRelationType(relationType);		
				
				try {
					analyseTaskControl.testAlgorithm(dto);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ServiceProvider.getInstance().getDefineService().getSarService().updateModulePanel();
			}
			else{
				logger.warn("No Approache selected");
			}
		}

	}
	
	private ModuleDTO getSelectedModule(){
		return ServiceProvider.getInstance().getDefineService().getSarService().getModule_SelectedInGUI();
	}

}