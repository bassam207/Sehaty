

-- Alter medical_files table to track source, related test results, and laboratory
ALTER TABLE medical_files
    ADD COLUMN source VARCHAR(50) DEFAULT 'USER_UPLOAD', -- Source of the file (USER_UPLOAD, LAB_UPLOAD, OTHER)
ADD COLUMN test_result_id UUID, -- Link to test result if applicable
ADD COLUMN source_laboratory_id UUID, -- Laboratory that uploaded the file
ADD COLUMN test_date TIMESTAMP; -- Date of the test associated with this file

-- Foreign key constraints
ALTER TABLE medical_files
    ADD CONSTRAINT fk_medicalfile_testresult
        FOREIGN KEY (test_result_id) REFERENCES test_results(id) ON DELETE SET NULL; -- Keep file even if test result is deleted

ALTER TABLE medical_files
    ADD CONSTRAINT fk_medicalfile_laboratory
        FOREIGN KEY (source_laboratory_id) REFERENCES laboratories(id) ON DELETE SET NULL; -- Keep file even if lab is deleted

-- Indexes to improve search and filtering
CREATE INDEX idx_medicalfiles_source ON medical_files(source); -- Filter by source
CREATE INDEX idx_medicalfiles_test_result ON medical_files(test_result_id); -- Quickly find files related to a test
CREATE INDEX idx_medicalfiles_laboratory ON medical_files(source_laboratory_id); -- Quickly find files uploaded by a lab

-- Optional: Consider CHECK constraint for source to avoid invalid values
ALTER TABLE medical_files
    ADD CONSTRAINT chk_medicalfiles_source
        CHECK (source IN ('USER_UPLOAD','LAB_UPLOAD','OTHER'));
