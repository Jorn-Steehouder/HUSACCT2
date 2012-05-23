package husacct.control.presentation.menubar;

import husacct.ServiceProvider;
import husacct.control.IControlService;
import husacct.control.ILocaleChangeListener;
import husacct.control.task.MainController;

import java.util.Locale;

import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

public class MenuBar extends JMenuBar{
	private static final long serialVersionUID = 1L;
	
	FileMenu fileMenu;
	DefineMenu defineMenu;
	AnalyseMenu analyseMenu;
	ValidateMenu validateMenu;
	LanguageMenu languageMenu;
	HelpMenu helpMenu;
	
	IControlService controlService;
	
	public MenuBar(MainController mainController){
		
		controlService = ServiceProvider.getInstance().getControlService();
		
		fileMenu = new FileMenu(mainController);
		defineMenu = new DefineMenu(mainController);
		analyseMenu = new AnalyseMenu(mainController);
		validateMenu = new ValidateMenu(mainController);
		languageMenu = new LanguageMenu(mainController);
		helpMenu = new HelpMenu(mainController);

		fileMenu.setMnemonic(getMnemonicKeycode("FileMenuMnemonic"));
		defineMenu.setMnemonic(getMnemonicKeycode("DefineMenuMnemonic"));
		analyseMenu.setMnemonic(getMnemonicKeycode("AnalyseMenuMnemonic"));
		validateMenu.setMnemonic(getMnemonicKeycode("ValidateMenuMnemonic"));
		languageMenu.setMnemonic(getMnemonicKeycode("LanguageMenuMnemonic"));
		helpMenu.setMnemonic(getMnemonicKeycode("HelpMenuMnemonic"));
		
		add(fileMenu);
		add(defineMenu);
		add(analyseMenu);
		add(validateMenu);
		add(languageMenu);
		add(helpMenu);
		
	}
	
	public void addListeners(){
		controlService.addLocaleChangeListener(new ILocaleChangeListener() {
			public void update(Locale newLocale) {
				fileMenu.setMnemonic(getMnemonicKeycode("FileMenuMnemonic"));
				defineMenu.setMnemonic(getMnemonicKeycode("DefineMenuMnemonic"));
				analyseMenu.setMnemonic(getMnemonicKeycode("AnalyseMenuMnemonic"));
				validateMenu.setMnemonic(getMnemonicKeycode("ValidateMenuMnemonic"));
				languageMenu.setMnemonic(getMnemonicKeycode("LanguageMenuMnemonic"));
				helpMenu.setMnemonic(getMnemonicKeycode("HelpMenuMnemonic"));
			}
		});
	}
	
	private int getMnemonicKeycode(String translatedString) {
		String mnemonicString = controlService.getTranslatedString(translatedString);
		int keyCode = KeyStroke.getKeyStroke(mnemonicString).getKeyCode();
		return keyCode;
	}
	
	public FileMenu getFileMenu(){
		return fileMenu;
	}
	
	public DefineMenu getDefineMenu(){
		return defineMenu;
	}
	
	public AnalyseMenu getAnalyseMenu(){
		return analyseMenu;
	}
	
	public ValidateMenu getValidateMenu(){
		return validateMenu;
	}
}
