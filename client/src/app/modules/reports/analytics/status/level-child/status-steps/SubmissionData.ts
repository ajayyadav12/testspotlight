export interface SubmissionData {
  id: number;
  process: string;
  adHoc: boolean;
  startTime: any;
  endTime: any;
  duration: number;
  status: string;

  stepSubmissionId: number;
  stepName: string;
  stepId: number;
  stepStartTime: any;
  stepEndTime: any;
  stepDuration: number;
  stepStatus: string;

  total: number;
  empty: number;

  warnings: number;
  records: number;
  errors: number;

  sender: string;
  receiver: string;
  processType: string;
  critical: boolean;
  parent: string;

  reportRangeStart: any;
  reportRangeEnd: any;
  reportDate: any;

  fileName: string;
}
