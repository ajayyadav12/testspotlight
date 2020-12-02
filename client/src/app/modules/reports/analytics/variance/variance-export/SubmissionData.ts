export interface SubmissionData {
  id: number;
  process: string;
  adHoc: boolean;
  startTime: any;
  endTime: any;
  duration: number;
  status: string;

  schedule: number;
  tolerance: number;
  avgPeriod: number;
  avgHist: number;
  comparison: number;
  total: number;
  empty: number;
  range: any;
  rangeCount: any;

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
