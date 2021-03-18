export interface NotificationRequest {
    processStepId: number,
    statusId: number,
    enableTextMessaging: boolean,
    escalationType: string,
    submissionType: string
}