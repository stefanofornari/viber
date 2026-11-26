# Notes for US-000069

- Errors are published on a read-only `errorProperty()` (`Exception`), never thrown or shown in a dialog; `null` means the last operation succeeded.
- Parse failures and I/O failures are both reported there; `save` with no story reports an `IllegalStateException`.
- A failed load keeps the current story; files are read and written as UTF-8.
- The story's note 4 (dirty reset) is not implemented: the dirty state belongs to US-000072.
