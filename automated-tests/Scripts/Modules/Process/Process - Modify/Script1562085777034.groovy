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

WebUI.delay(5)

WebUI.mouseOver(findTestObject('Page_Spotlight/User/first row'))

WebUI.click(findTestObject('Page_Spotlight/i_modify record'))

WebUI.comment('Add user')

WebUI.click(findTestObject('Page_Spotlight/Process/_Menu/span_Users'))

WebUI.click(findTestObject('Page_Spotlight/Process/label_Select User'))

WebUI.setText(findTestObject('Page_Spotlight/input_dropdown filter'), 'Spotlight Testing Admin')

WebUI.click(findTestObject('Page_Spotlight/Process/li_ first item'))

WebUI.delay(2)

WebUI.sendKeys(null, Keys.chord(Keys.ENTER), FailureHandling.STOP_ON_FAILURE)

WebUI.delay(1)

WebUI.comment('Add Step')

WebUI.click(findTestObject('Page_Spotlight/Process/_Menu/span_steps'))

WebUI.setText(findTestObject('Page_Spotlight/Process/input_stepname'), 'test')

WebUI.waitForElementClickable(findTestObject('Page_Spotlight/Process/btn_add'), 0)

WebUI.delay(1)

WebUI.click(findTestObject('Page_Spotlight/Process/btn_add'))

