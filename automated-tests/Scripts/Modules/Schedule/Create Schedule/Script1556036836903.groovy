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

WebUI.click(findTestObject('Page_Spotlight/a_Schedule'))

WebUI.click(findTestObject('Page_Spotlight/Schedule/button_New Schedule'))

WebUI.verifyElementPresent(findTestObject('Page_Spotlight/Schedule/Form/h3_Custom'), 0)

WebUI.click(findTestObject('Page_Spotlight/Schedule/Form/label_no end date'))

WebUI.click(findTestObject('Page_Spotlight/Schedule/Form/label_Select Process'))

WebUI.click(findTestObject('Page_Spotlight/Schedule/li_Test-AT'))

WebUI.click(findTestObject('Page_Spotlight/Schedule/Form/button_Add'))

WebUI.click(findTestObject('Page_Spotlight/Schedule/Form/input_startTime'))

WebUI.click(findTestObject('Page_Spotlight/Schedule/Form/span_arrow down hour'))

WebUI.sendKeys(findTestObject(null), Keys.chord(Keys.TAB))

WebUI.click(findTestObject('Page_Spotlight/Schedule/Form/input_endTime'))

WebUI.click(findTestObject('Page_Spotlight/Schedule/Form/span_arrow up hour'))

WebUI.sendKeys(findTestObject(null), Keys.chord(Keys.TAB))

WebUI.waitForElementClickable(findTestObject('Page_Spotlight/Schedule/Form/button_save'), 0)

WebUI.click(findTestObject('Page_Spotlight/Schedule/Form/button_save'))

WebUI.verifyTextPresent('Get Ready! New Schedule!', false)

WebUI.delay(3, FailureHandling.STOP_ON_FAILURE)

WebUI.callTestCase(findTestCase('log-out'), [:], FailureHandling.STOP_ON_FAILURE)

