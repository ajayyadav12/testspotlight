export class SubmissionCommon {
  public static submissionStatusColor(status) {
    let color;
    switch (status) {
      case 'success':
        color = '#00bf6f';
        break;
      case 'in progress':
        color = '#027ad9';
        break;
      case 'warning':
        color = '#ffa600';
        break;
      case 'failed':
        color = '#f44336';
        break;
      default:
        break;
    }
    return color;
  }
}
