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
import com.kms.katalon.core.webui.common.WebUiCommonHelper as WebUiCommonHelper
import org.openqa.selenium.WebElement as WebElement

WebUI.callTestCase(findTestCase('log-in'), [:], FailureHandling.STOP_ON_FAILURE)

WebUI.click(findTestObject('Page_Spotlight/Menu/a_Process'))

WebUI.click(findTestObject('Page_Spotlight/Process/btn_new Process'))

WebUI.setText(findTestObject('Page_Spotlight/Process/input_name'), '000')

WebUI.click(findTestObject('Page_Spotlight/Process/label_feed type'))

WebUI.click(findTestObject('Page_Spotlight/Process/li_ first item'))

WebUI.waitForElementNotPresent(findTestObject('Page_Spotlight/Process/li_ first item'), 0)

WebUI.click(findTestObject('Page_Spotlight/Process/label_process type'))

WebUI.click(findTestObject('Page_Spotlight/Process/li_ first item'))

WebUI.waitForElementClickable(findTestObject('Page_Spotlight/Process/btn_save'), 0)

WebUI.click(findTestObject('Page_Spotlight/Process/btn_save'), FailureHandling.STOP_ON_FAILURE)

WebUI.callTestCase(findTestCase('Modules/Process/Process - Modify'), [:], FailureHandling.STOP_ON_FAILURE)

WebUI.callTestCase(findTestCase('log-out'), [:], FailureHandling.STOP_ON_FAILURE)

