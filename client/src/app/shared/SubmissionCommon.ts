import { Colors } from './Constants/Colors';

export class SubmissionCommon {
  public static submissionStatusColor(status) {
    let color;
    switch (status) {
      case 'success':
        color = Colors.green;
        break;
      case 'in progress':
        color = Colors.blue;
        break;
      case 'warning':
        color = Colors.yellow;
        break;
      case 'failed':
        color = Colors.red;
        break;
      case 'long running':
        color = Colors.voilet;
        break;
      default:
        break;
    }
    return color;
  }

  public static submissionStatusColorCode(status) {
    let colorCode;
    switch (status) {
      case 'success':
        colorCode = '#88c6ba';
        break;
      case 'in progress':
        colorCode = '#AEB6BF';
        break;
      case 'warning':
        colorCode = '#F9E79F';
        break;
      case 'failed':
        colorCode = '#ff9b9b';
        break;
      case 'long running':
        colorCode = Colors.voilet;
        break;
      default:
        break;
    }
    return colorCode;
  }
}
