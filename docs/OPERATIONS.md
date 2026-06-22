# Operation Types Reference

Each entity can be configured with one or more operation types. The generator produces the corresponding controller endpoints, service methods, and repository queries.

## Standard CRUD

### CREATE
- **Endpoint**: `POST /api/{entity}s`
- **Generated**: Controller method, Service `create` with field mapping and FK resolution, CreateRequest DTO

### READ
- **Endpoint**: `GET /api/{entity}s` and `GET /api/{entity}s/{id}`
- **Generated**: Controller methods, Service `getById` and `getAll`, Repository `findById`/`findAll`

### UPDATE
- **Endpoint**: `PUT /api/{entity}s/{id}`
- **Generated**: Controller method, Service `update` with partial field update and FK resolution, UpdateRequest DTO

### DELETE
- **Endpoint**: `DELETE /api/{entity}s/{id}`
- **Generated**: Controller method, Service `delete` with existence check

## Query Operations

### SEARCH
- **Endpoint**: `GET /api/{entity}s/search?query=term`
- **Generated**: Repository `findByNameContainingIgnoreCase`, Service `search`, Controller with `@RequestParam`

### PAGINATION
- **Endpoint**: `GET /api/{entity}s/paged?page=0&size=20&sort=id,asc`
- **Generated**: Controller with page/size/sort params (max 100 per page), Service using `Pageable`, Repository extends `JpaSpecificationExecutor`

## Bulk Operations

### BULK_INSERT
- **Endpoint**: `POST /api/{entity}s/bulk`
- **Limit**: 500 items per request
- **Generated**: Validates count, maps all requests to entities, `saveAll`

### BULK_UPDATE
- **Endpoint**: `PUT /api/{entity}s/bulk`
- **Generated**: Accepts list of UpdateRequest DTOs

### BULK_DELETE
- **Endpoint**: `DELETE /api/{entity}s/bulk`
- **Body**: `[1, 2, 3]` (list of IDs)
- **Generated**: `deleteAllById`

## Soft Delete / Restore

### SOFT_DELETE
- **Endpoint**: `DELETE /api/{entity}s/{id}/soft`
- **Requires**: Entity must have `deleted` (boolean) and `deletedAt` (timestamp) fields
- **Generated**: Repository `softDelete` query, `findAllActive` excluding soft-deleted records

### RESTORE
- **Endpoint**: `PATCH /api/{entity}s/{id}/restore`
- **Generated**: Repository `restore` query setting `deleted=false` and `deletedAt=null`

## Export Operations

### EXPORT_CSV
- **Endpoint**: `GET /api/{entity}s/export/csv`
- **Response**: Downloaded CSV file with dynamic headers based on entity fields

### EXPORT_EXCEL
- **Endpoint**: `GET /api/{entity}s/export/excel`
- **Response**: `.xlsx` file (Apache POI)

### EXPORT_PDF
- **Endpoint**: `GET /api/{entity}s/export/pdf`
- **Response**: PDF file (OpenPDF)

## Import Operations

### IMPORT_CSV
- **Endpoint**: `POST /api/{entity}s/import/csv`
- **Input**: Multipart file upload
- **Response**: `"Imported: X, Errors: Y"`

### IMPORT_EXCEL
- **Endpoint**: `POST /api/{entity}s/import/excel`
- **Input**: Multipart file upload

## File Operations

### FILE_UPLOAD
- **Endpoint**: `POST /api/{entity}s/{id}/upload`
- **Input**: Multipart file

### FILE_DOWNLOAD
- **Endpoint**: `GET /api/{entity}s/{id}/download`
- **Response**: Binary file download

## Advanced Operations

### STATUS_TRANSITION
- **Endpoint**: `PATCH /api/{entity}s/{id}/status?status=APPROVED`
- **Requires**: Entity must have a `status` (String) field
- **Generated**: Service validates transition, updates status

### AUDIT_LOG
- **Generated**: JPA entity listener tracking field-level changes in a separate audit table

### VERSIONING
- **Generated**: `@Version` field for optimistic locking

### WEBHOOK_INTEGRATION
- **Generated**: Event publisher triggering async webhook calls on entity changes

## Example Configuration

```json
{
  "name": "Order",
  "fields": [...],
  "operations": [
    {"operationType": "CREATE", "enabled": true},
    {"operationType": "READ", "enabled": true},
    {"operationType": "UPDATE", "enabled": true},
    {"operationType": "SOFT_DELETE", "enabled": true},
    {"operationType": "RESTORE", "enabled": true},
    {"operationType": "PAGINATION", "enabled": true},
    {"operationType": "EXPORT_CSV", "enabled": true}
  ]
}
```
