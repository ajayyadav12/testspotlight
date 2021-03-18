export interface ProcessExportRequest {
  id: number;
  processId: number;
  userId: number;
  settings: string[];
  state: string;
  requested: string;
}
