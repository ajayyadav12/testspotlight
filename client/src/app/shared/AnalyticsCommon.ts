export class AnalyticsCommon {
  /**
   * Generate CSV file, using columns[] and submissions[]
   * Remove unneccessary data (keys: fileName)
   * Convert Object to CSV and download
   */
  public static downloadCSVFile(submissions, columns) {
    let data = submissions;

    // use first data point to get element keys
    let elements = Object.keys(data[0]);

    // remove element: fileName
    elements.pop();

    // create new row for each submission in data[]
    let rows = [];
    data.map(row => {
      let rowData = [];
      elements.map(fieldName => {
        rowData.push(row[fieldName].toString());
      });
      rows.push(rowData.join(','));
    });

    // create string of headers separated by commas
    // add row for headers to beginning of rows[]
    const headers = columns;
    rows.unshift(headers.join(','));

    // separate each array index by return and new line
    let csvArray = rows.join('\r\n');

    // generate csv file
    var a = document.createElement('a');
    var blob = new Blob([csvArray], { type: 'text/csv' }),
      url = window.URL.createObjectURL(blob);

    // imitate user interaction for download
    a.href = url;

    // create unique file name: using process name and date report generated
    a.download = data[0].fileName + '.csv';

    // download prepared file
    a.click();
    window.URL.revokeObjectURL(url);
    a.remove();
  }
}
