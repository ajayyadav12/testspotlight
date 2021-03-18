const TODAY = new Date();
export class DateCommon {

  /**
   * Find the the difference in days, hours, minutes and seconds betweeen two dates
   * @param startTime
   * @param endTime
   * @param isShortVersion if true, string date format would be in "3m 12s" format
   */
  public static dateDifference(startTime, endTime, isShortVersion: boolean, duration = null) {
    let labels: { days: string; hours: string; minutes: string; seconds: string };

    if (isShortVersion) {
      labels = {
        days: 'd ',
        hours: 'h ',
        minutes: 'm ',
        seconds: 's'
      };
    } else {
      labels = {
        days: ' days, ',
        hours: ' hours, ',
        minutes: ' minutes, ',
        seconds: ' seconds'
      };
    }
    if ((!startTime || !endTime) && !duration) {
      return '';
    }
    startTime = new Date(startTime);
    endTime = new Date(endTime);
    const msec = duration ? duration * 1000 : endTime.getTime() - startTime.getTime();
    let mins = Math.floor(msec / 60000);
    let hrs = Math.floor(mins / 60);
    const days = Math.floor(hrs / 24);
    mins = mins % 60;
    hrs = hrs % 24;
    const secs = (msec / 1000) % 60;
    let finalResult = '';
    if (days !== 0) {
      finalResult += days + labels.days;
    }
    if (hrs !== 0) {
      finalResult += hrs + labels.hours;
    }
    if (mins !== 0) {
      finalResult += mins + labels.minutes;
    }
    if (secs !== 0) {
      finalResult += Math.round(secs) + labels.seconds;
    }
    return finalResult;
  }

  /**
   * Some datetime values have past dates, this is to update it to today's date so TimeZone calculation works fine.
   * @param tempDate
   */
  public static adjustDate(tempDate: Date) {
    tempDate.setFullYear(TODAY.getFullYear());
    tempDate.setMonth(TODAY.getMonth());
    tempDate.setDate(TODAY.getDate());
  }

}
