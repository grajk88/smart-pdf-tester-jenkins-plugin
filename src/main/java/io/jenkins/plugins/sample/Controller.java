package io.jenkins.plugins.sample;


import java.awt.Desktop;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class Controller {

	@FXML
	private Button uploadPDFButton; // name should be same as the fx:id

	@FXML
	private TextField pdfLocation;

	@FXML
	private TextField dataSheetLocation;

	@FXML
	private CheckBox visualCheck;

	@FXML
	private Text message;

	@FXML
	private ImageView loadingGif;

	private static ExtentReports extent;

	@FXML
	public void uploadPDFDocument(ActionEvent event) throws Exception {

		try {
			
			

			// Extent Reports

			extent = new ExtentReports(System.getProperty("user.dir") + "//Test-Report.html", true);

			// Form Handling

			String apiKeyString = "";

			String pdfLocationString = pdfLocation.getText().toString();

			String dataSheetPath = dataSheetLocation.getText().toString();

			// String filepath = System.getProperty("user.dir") + "/pdfs/Invoice_1.pdf";

			String filepath = pdfLocation.getText().toString();

			String fileName = null;

			File file = new File(filepath);

			File[] files = null;

			if (filepath.contains(".pdf")) {

				fileName = file.getName().replaceAll(".pdf", "");

			} else {

				files = file.listFiles();

				for (File f : files) {

					System.out.println(f.getName());
				}

			}

			String dataSheetName = null;
						

			/*
			 * PDF Stripper Handler For Ad-hoc PDF Validation
			 */

			try {

				for (File f : files) {

					int page = 0;

					int x = 0;
					int y = 0;
					int width = 0;
					int height = 0;

					ExtentTest test = extent.startTest(f.getName(), f.getName());

					test.log(LogStatus.INFO, "PDF File Name: " + f.getName());

					fileName = f.getName();

					// Loading an existing document
					PDDocument document = PDDocument.load(f);

					// Instantiate PDFTextStripper class
					PDFTextStripper pdfStripper = new PDFTextStripper();

					// Retrieving text from PDF document
					String strippedText = pdfStripper.getText(document);

					// System.out.println("Stripped Text: " + strippedText);

					// dataSheetName = System.getProperty("user.dir") + "/DataSheet.xlsx";

					dataSheetName = dataSheetPath;

					XSSFWorkbook workbook = new XSSFWorkbook(dataSheetName);

					XSSFSheet sheet = workbook.getSheet("Test_Data"); // Get data as per sheet name

					boolean result;

					String textToBeValidated;

					String columnName;

					message.setVisible(true);

					message.setText("Initiating Tests...");

					for (Row row : sheet) { // For each Row.

						Cell cell = row.getCell(0); // Get the Cell at the Index / Column you want.

						if (cell.getStringCellValue().equalsIgnoreCase(fileName)) {

							message.setText("Initiating Tests for " + fileName);

							for (int i = 1; i <= cell.getRow().getLastCellNum() - 1; i++) {

								columnName = cell.getSheet().getRow(0).getCell(i).getRichStringCellValue().toString();

								textToBeValidated = cell.getRow().getCell(i).toString();

								try {

									PDFTextStripperByArea textStripper = new PDFTextStripperByArea();

									XSSFSheet sheet2 = workbook.getSheet("Sheet3"); // Get data as per sheet name

									String coordinates;

									String columnName2;

									for (Row row2 : sheet2) { // For each Row.

										Cell cell2 = row2.getCell(0); // Get the Cell at the Index / Column you want.

										if (cell2.getStringCellValue().equalsIgnoreCase(fileName)) {

											for (int j = 1; j <= cell2.getRow().getLastCellNum() - 1; j++) {

												columnName2 = cell2.getSheet().getRow(0).getCell(i)
														.getRichStringCellValue().toString();

												coordinates = cell2.getRow().getCell(i).toString();

												System.out.println(coordinates);

												ArrayList<String> items = new ArrayList<String>(
														Arrays.asList(coordinates.split("\\s*,\\s*")));

												for (String str : items) {

													System.out.println(str);

													x = Integer.parseInt(items.get(0));
													y = Integer.parseInt(items.get(1));
													width = Integer.parseInt(items.get(2));
													height = Integer.parseInt(items.get(3));

												}

											}

											System.out.println(
													"X " + x + " Y " + y + " Height " + height + " Width " + width);

											Rectangle2D rect = new java.awt.geom.Rectangle2D.Float(x, y, width, height);

											textStripper.addRegion("region", rect);

											PDPage docPage = document.getPage(page);

											textStripper.extractRegions(docPage);

											String textForRegion = textStripper.getTextForRegion("region");

											System.out.println("Region Extracted ----> " + textForRegion);

											result = textForRegion.contains(textToBeValidated);

											if (result == true) {

												test.log(LogStatus.PASS, textToBeValidated + " is available");

											} else {

												test.log(LogStatus.FAIL,
														"Expected: <b>" + textToBeValidated
																+ "</b> <br/>Actual: <b>" + textForRegion + "</b>");
											}

										}
									}

								} catch (Exception e) {

									e.printStackTrace();

								}

							}
						}

					}

					// Closing the document

					document.close();

					extent.endTest(test);

					extent.flush();

				}

				// Visual Check Begins

				if (visualCheck.isSelected()) {

					try {

						// extent = new ExtentReports(System.getProperty("user.dir") +
						// "//Applitools-Test-Report.html", true);

						// ExtentTest test = extent.startTest("Visual Check in progress...");

						// test.log(LogStatus.INFO, "In progress");

						message.setText("Initiating Visual Check for " + fileName);

						apiKeyString = "H98dL101AYfejk59Dm32uZABni105YI3nVADSMMvMb35CK5Y110";

						String command = String.format("java -jar /Users/giridhar/Downloads/ImageTester.jar -k "
								+ apiKeyString + " -f " + filepath);

						Process process = Runtime.getRuntime().exec(command);

						process.waitFor();

						String stream = IOUtils.toString(process.getInputStream(), "UTF-8");

						if (stream != null && stream.contains("Mismatch")) {

							message.setVisible(true);

							message.setText("Mismatches Found. Navigate to Applitools to view the results...");

						} else if (stream != null && stream.contains("Nothing to test!")) {

							message.setVisible(true);

							message.setText(
									"There is some issue in Uploading the PDF. Can you check the path of the PDF?");

						} else if (stream != null && stream.contains("[New]") || stream.contains("[Passed]")) {

							message.setVisible(true);

							message.setText("Upload Completed...");

						}

						// extent.endTest(test);

					} catch (Exception e) {

						message.setVisible(true);

						message.setText(
								"There is some Problem in uploading the PDF. Try again and if issue persists, contact QE Enablers");

					}

				}

				// Visual Check Ends

				message.setVisible(true);

				message.setText("Test Execution Completed");
			

			} catch (Exception e) {
				
				e.printStackTrace();
				
				message.setVisible(true);

				message.setText(
						"Test Execution NOT Completed. There was some error. Please contact the Administrator.");
			}

			// extent.flush();

		}

		catch (

		Exception e) {
			System.out.println(e);
		}

	}
	
	@FXML
	public void openPDFTestResults(ActionEvent event) throws Exception {
		
        Desktop.getDesktop().browse(new URI("file://"+System.getProperty("user.dir")+ "/Test-Report.html"));
		
	}
	
	@FXML
	public void launchApplitools(ActionEvent event) throws Exception {
		
        Desktop.getDesktop().browse(new URI("https://eyes.applitools.com/app/test-results/"));
		
	}
}
