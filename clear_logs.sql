/*
============================================================
 SCRIPT CLEAR LOGS
============================================================
*/

USE master;
GO

-- Clear transaction log của db_thuvien
ALTER DATABASE db_thuvien SET RECOVERY SIMPLE;
GO

DBCC SHRINKFILE (db_thuvien_log, 1);
GO

ALTER DATABASE db_thuvien SET RECOVERY FULL;
GO

-- Delete backup history
EXEC msdb.dbo.sp_delete_backuphistory @oldest_date = '2025-01-01';
GO

PRINT '';
PRINT '*** ĐÃ CLEAR LOG THÀNH CÔNG ***';
GO
