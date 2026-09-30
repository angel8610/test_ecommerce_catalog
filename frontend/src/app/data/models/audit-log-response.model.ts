export interface AuditLogResponse {

  auditLogId: number;
  operation: string;
  status: string;
  durationMs: number,
  registerDate: Date;
  createdBy: string;
  error: string;


}
