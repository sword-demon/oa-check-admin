-- V16: Allow multiple active versions of the same process template key.
--
-- Versioning creates a new draft row with the same template_key. The previous
-- unique key only covered (template_key, deleted), so new-version creation
-- failed before the draft could be edited.
ALTER TABLE biz_process_template
    DROP INDEX uk_template_key,
    ADD UNIQUE KEY uk_template_key_version (template_key, version, deleted);
