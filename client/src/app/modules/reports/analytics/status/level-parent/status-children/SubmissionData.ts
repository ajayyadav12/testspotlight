export interface SubmissionData {
  id: number;
  process: string;
  adHoc: boolean;
  startTime: any;
  endTime: any;
  duration: number;
  status: string;

  expectedChildren: number;
  incomingChildren: number;

  childSubmissionId: number;
  childName: string;
  childFeedType: string;
  childId: number;
  childStartTime: any;
  childEndTime: any;
  childDuration: number;
  childStatus: string;

  warnings: number;
  records: number;
  errors: number;

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
