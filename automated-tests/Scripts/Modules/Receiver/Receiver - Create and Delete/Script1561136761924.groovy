import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys

WebUI.callTestCase(findTestCase('log-in'), [:], FailureHandling.STOP_ON_FAILURE)

WebUI.click(findTestObject('Page_Spotlight/Menu/a_Receiver'))

WebUI.click(findTestObject('Page_Spotlight/Receiver/btn_new receiver'))

WebUI.comment('Create Receiver')

WebUI.sendKeys(findTestObject('Page_Spotlight/Receiver/input_name'), '000')

WebUI.click(findTestObject('Page_Spotlight/Receiver/label_select close phase'))

WebUI.click(findTestObject('Page_Spotlight/li_ first item'))

WebUI.delay(1)

WebUI.click(findTestObject('Page_Spotlight/Receiver/btn_save'), FailureHandling.STOP_ON_FAILURE)

WebUI.comment('Delete Receiver')

WebUI.delay(5)

WebUI.mouseOver(findTestObject('Page_Spotlight/Receiver/first row'))

WebUI.click(findTestObject('Page_Spotlight/i_delete record'))

WebUI.acceptAlert()

WebUI.callTestCase(findTestCase('log-out'), [:], FailureHandling.STOP_ON_FAILURE)

WebUI.closeBrowser()

