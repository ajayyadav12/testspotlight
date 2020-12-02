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

WebUI.callTestCase(findTestCase('log-in'), [:], FailureHandling.STOP_ON_FAILURE)

WebUI.click(findTestObject('Page_Spotlight/Menu/a_Process'))

WebUI.delay(5)

WebUI.mouseOver(findTestObject('Page_Spotlight/User/first row'))

WebUI.click(findTestObject('Page_Spotlight/i_modify record'))

WebUI.comment('Delete Steps')

WebUI.click(findTestObject('Page_Spotlight/Process/_Menu/span_steps'))

while (true) {
    try {
        WebUI.mouseOver(findTestObject('Page_Spotlight/Process/first row - generic'))

        WebUI.click(findTestObject('Page_Spotlight/i_delete record'))

        WebUI.acceptAlert()

        WebUI.delay(2)
    }
    catch (Exception e) {
        break
    } 
}

WebUI.comment('Delete Users')

WebUI.click(findTestObject('Page_Spotlight/Process/_Menu/span_Users'))

WebUI.waitForElementClickable(findTestObject('Page_Spotlight/Process/first row - generic'), 0)

WebUI.mouseOver(findTestObject('Page_Spotlight/Process/first row - generic'))

WebUI.click(findTestObject('Page_Spotlight/i_delete record'))

WebUI.acceptAlert()

WebUI.click(findTestObject('Page_Spotlight/i_back'))

WebUI.delay(5)

WebUI.mouseOver(findTestObject('Page_Spotlight/User/first row'))

WebUI.click(findTestObject('Page_Spotlight/i_delete record'))

WebUI.acceptAlert()

WebUI.callTestCase(findTestCase('log-out'), [:], FailureHandling.STOP_ON_FAILURE)

