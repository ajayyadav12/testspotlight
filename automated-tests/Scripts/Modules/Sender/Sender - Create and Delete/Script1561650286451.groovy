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

WebUI.click(findTestObject('Page_Spotlight/Menu/a_Sender'))

WebUI.click(findTestObject('Page_Spotlight/Sender/btn_new sender'))

WebUI.comment('Create Sender')

WebUI.sendKeys(findTestObject('Page_Spotlight/Sender/input_name'), '000')

WebUI.click(findTestObject('Page_Spotlight/Sender/label_ Close Phase'))

WebUI.click(findTestObject('Page_Spotlight/li_ first item'))

WebUI.delay(1)

WebUI.click(findTestObject('Page_Spotlight/Sender/label_ App Owner'))

WebUI.click(findTestObject('Page_Spotlight/li_ first item'))

WebUI.waitForElementClickable(findTestObject('Page_Spotlight/Sender/btn_save'), 0)

WebUI.delay(1)

WebUI.click(findTestObject('Page_Spotlight/Sender/btn_save'), FailureHandling.STOP_ON_FAILURE)

WebUI.comment('Delete Sender')

WebUI.delay(5)

WebUI.mouseOver(findTestObject('Page_Spotlight/Sender/first row'))

WebUI.click(findTestObject('Page_Spotlight/i_delete record'))

WebUI.acceptAlert()

WebUI.callTestCase(findTestCase('log-out'), [:], FailureHandling.STOP_ON_FAILURE)

