export interface SubmissionData {
  id: number;
  process: string;
  adHoc: boolean;
  startTime: any;
  endTime: any;
  duration: number;
  status: string;

  total: number;
  empty: number;

  sender: string;
  receiver: string;
  processType: string;
  critical: boolean;

  reportRangeStart: any;
  reportRangeEnd: any;
  reportDate: any;

  fileName: string;
}
