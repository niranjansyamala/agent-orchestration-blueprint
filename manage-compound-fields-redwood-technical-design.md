# Manage Compound Fields in Redwood

## Technical Design Document

## 1. Purpose

This document describes the UI architecture, VBCS/Redwood components, page state, REST integration, action chains, and implementation assumptions required to build a Redwood-style **Manage Compound Fields** page matching the supplied screenshot.

The target experience is a lightweight management page that:

- loads compound fields for a given context code
- displays them in a readable Redwood list/card layout
- supports add, edit, and delete
- keeps create/edit interactions inside a drawer so the main page remains a management overview

## 2. Target UX Summary

The screenshot shows a simple management page with:

- a page title area: `Manage Compound Fields`
- a content card titled `Compound Fields`
- an `Add` button in the top-right of the card
- a vertical list of compound-field entries
- each entry rendered as a two-column read-only summary
- an edit icon on each entry
- clear grouping and spacing consistent with Redwood

Recommended interaction model:

- main page shows the current list of compound fields for one context
- `Add` opens a right-side modal drawer in create mode
- edit icon opens the same drawer in edit mode
- delete is exposed inside the drawer footer for edit mode, not on the main list, to reduce accidental deletion

## 3. Recommended Page Pattern

### 3.1 Main page shell

Use `oj-sp-general-overview-page` as the page template for the management page shell.

Why:

- VBLens MCP guidance describes `oj-sp-general-overview-page` as the Redwood page template for a simple business-object overview page with a required single `slot="main"` wrapper, page title, and in-flow back navigation support.
- The page is primarily a management overview and does not require page-level Save/Cancel in the header.
- Create and edit are isolated to a drawer, so the page itself remains list-oriented rather than a full-page transaction.

Design note:

- If the final UX changes to a full-page create/edit flow with page-level Save/Cancel, the main template should be re-evaluated and likely moved to a create/edit page template instead of the overview template.

### 3.2 Main content container

Inside the overview page `main` slot:

- `oj-hcm-page-container`
- `oj-hcm-header`
- Redwood-styled panel/card container for the list content

This aligns with the `oj-sp-general-overview-page` MCP guidance that calls out the use of `oj-hcm-page-container` and `oj-hcm-header` for page-level structure.

## 4. UI Component Inventory

This section lists the recommended components and what each one is responsible for.

### 4.1 Page shell and structure

| Area | Component | Purpose | Notes |
|---|---|---|---|
| page shell | `oj-sp-general-overview-page` | Redwood overview page container | Put all page content under a single `slot="main"` wrapper |
| page body | `oj-hcm-page-container` | page-level HCM container | supports page framework behavior |
| page heading | `oj-hcm-header` | Redwood page heading area | use translation-backed title/subtitle |
| content card | Redwood panel/card wrapper | visual grouping for the list | can be standard container markup styled with Redwood utility classes |

### 4.2 Main actions

| Area | Component | Purpose | Notes |
|---|---|---|---|
| add action | `oj-c-button` | open create drawer | use `chroming="outlined"` or `solid` based on page style |
| row edit | `oj-c-button` | icon-only edit trigger | use `display="icons"` and always provide accessible `label` |

`oj-c-button` is preferred over legacy `oj-button` because VBLens MCP marks `oj-c-button` as the superseding component.

### 4.3 Repeating list of compound fields

Recommended implementation:

- `oj-list-view` bound to a data provider or
- a lightweight repeat pattern over fetched items if the list is guaranteed to stay small

Preferred option: `oj-list-view`

Why:

- VBLens MCP describes `oj-list-view` as the standard interactive list component for list or grid rendering.
- It supports scalable rendering if the number of compound fields grows.
- It keeps the implementation ready for selection, templating, and future enhancements.

Row content inside each list item should use:

- simple Redwood text layout
- responsive two-column summary block
- edit icon aligned to the far right

### 4.4 Create and edit experience

Use `oj-drawer-popup` for add/edit.

Why:

- VBLens MCP describes `oj-drawer-popup` as the overlay drawer component for temporary interactions such as forms.
- It supports modal behavior, close handling, and accessibility.
- It matches the desired UX of keeping the user on the management page while editing.

Recommended drawer settings:

- `edge="end"`
- `modality="modal"`
- `auto-dismiss="none"` for form safety
- `close-gesture="none"` if there are unsaved changes checks
- explicit `aria-label` or `aria-labelledby`

### 4.5 Drawer form controls

Use Redwood form components:

| Field Type | Component | Purpose |
|---|---|---|
| field name | `oj-c-input-text` | compound field display name |
| field category | `oj-c-select-single` | choose category |
| multivalued flag | `oj-c-select-single` or `oj-c-checkboxset` | choose Yes/No |
| member fields | `oj-c-checkboxset` or multi-select pattern | choose one or more underlying fields |
| drawer action buttons | `oj-c-button` | Save, Cancel, Delete |

Recommended control choices:

- use `oj-c-input-text` for the name because it is the current Redwood text input component
- use `oj-c-select-single` for category because the user selects one category
- use `oj-c-select-single` for multivalued Yes/No to keep the interaction compact and explicit
- use `oj-c-checkboxset` for selecting multiple member fields because a compound field is composed of multiple source fields

## 5. Page Layout Breakdown

## 5.1 Header section

Content:

- page title
- optional subtitle showing the current context code
- back navigation if this page is launched from a calling flow

Suggested layout behavior:

- title and navigation belong in the page shell/header
- translation action from the screenshot is outside this page scope unless the host shell already provides it

## 5.2 Main card section

Card header:

- section title: `Compound Fields`
- add button aligned right

Card body:

- list of compound field rows
- empty state when no rows are returned
- optional busy indicator while data loads

## 5.3 Row design

Each row should show:

- field name
- field category
- multivalued field flag
- list of member fields
- edit icon button

Recommended responsive behavior:

- desktop/tablet: two-column summary layout
- mobile: stacked single-column layout

## 5.4 Drawer layout

Drawer sections:

- header: create/edit mode title
- body: form controls
- footer: Cancel, Save, and Delete in edit mode

Recommended footer actions:

- `Cancel`: closes drawer, with dirty-check if needed
- `Save`: calls create or update service
- `Delete`: shown only in edit mode, opens confirmation, then calls delete service

## 6. Data Model

The exact REST payloads are not yet defined in the request, so the UI design should be built around a normalized client-side view model.

### 6.1 Suggested list item view model

```json
{
  "fieldId": 1001,
  "fieldName": "Certification Info",
  "fieldCategoryCode": "LICENSES_AND_CERTIFICATIONS",
  "fieldCategoryLabel": "Licenses and Certifications",
  "multivaluedFlag": true,
  "multivaluedLabel": "Yes",
  "fields": [
    {
      "fieldCode": "CERTIFICATION_NAME",
      "fieldLabel": "Certification Name"
    },
    {
      "fieldCode": "ISSUED_BY",
      "fieldLabel": "Issued By"
    }
  ],
  "contextCode": "ORA_SUBMISSION"
}
```

### 6.2 Suggested drawer form state

```json
{
  "mode": "create",
  "fieldId": null,
  "contextCode": "ORA_SUBMISSION",
  "fieldName": "",
  "fieldCategoryCode": null,
  "multivaluedFlag": false,
  "selectedFieldCodes": []
}
```

## 7. REST Service Mapping

## 7.1 Load compound fields

Service:

- `GET /hcmRestApi/resources/latest/recruitingUIGridViewCompoundFields?finder=findByContextCode;ContextCode=ORA_SUBMISSION`

Usage:

- called on `vbEnter`
- called again after successful create, update, or delete

UI behavior:

- set busy state before call
- map response to list view model
- clear busy state after success/failure
- show empty state when no items are returned

## 7.2 Create compound field

Service:

- `POST /hcmRestApi/resources/latest/recruitingUIGridViewCompoundFields`

Usage:

- called from drawer `Save` in create mode

UI behavior:

- validate drawer form
- submit payload
- close drawer on success
- refresh list
- show confirmation toast/notification

## 7.3 Update compound field

Provided contract:

- `POST /hcmRestApi/resources/latest/recruitingUIGridViewCompoundFields`

Important design note:

- the provided update service definition is ambiguous because it duplicates the create resource and still says it is used for creating compound fields
- the update contract must be confirmed before implementation

Open items to verify:

- whether update is actually `POST`, `PATCH`, or `PUT`
- whether `fieldId` is expected in the payload
- whether update uses an item resource such as `.../{FieldId}`
- whether the payload is full-replacement or partial-update

Current implementation assumption for design purposes only:

- save action routes by mode
- create mode calls create service
- edit mode calls the update service once the final API contract is confirmed

## 7.4 Delete compound field

Service:

- `DELETE /hcmRestApi/resources/latest/recruitingUIGridViewCompoundFields/{FieldId}`

Usage:

- called from drawer in edit mode after user confirmation

UI behavior:

- prompt for confirmation
- call delete
- close drawer on success
- refresh list
- show confirmation notification

## 8. Page Variables

Per VBLens MCP guidance, page state should live in page variables and action chains should manipulate that state.

Recommended page variables:

| Variable | Type | Purpose |
|---|---|---|
| `contextCode` | `string` | current context, default `ORA_SUBMISSION` or passed from caller |
| `isPageBusy` | `boolean` | page/list loading state |
| `compoundFieldsSDP` | `vb/ServiceDataProvider` or `vb/ArrayDataProvider2` | list binding target |
| `compoundFields` | `object[]` | normalized list data |
| `selectedCompoundField` | `object` | currently edited item |
| `isDrawerOpen` | `boolean` | controls drawer open state |
| `drawerMode` | `string` | `create` or `edit` |
| `drawerForm` | `object` | current form values |
| `isDrawerBusy` | `boolean` | save/delete busy state |
| `fieldCategoryOptionsDP` | `vb/ArrayDataProvider2` | category LOV |
| `memberFieldOptionsDP` | `vb/ArrayDataProvider2` | selectable member fields LOV |
| `formValid` | `boolean` | optional save enablement state |
| `messages` | `object[]` | custom page or drawer messages |

## 9. Data Provider Strategy

Recommended approach:

- use a simple `compoundFields` array plus `vb/ArrayDataProvider2` for the main list if the service returns the full small dataset for one context
- reserve `vb/ServiceDataProvider` for future scaling if the page moves toward larger datasets or server-driven filtering

Reasoning:

- this page appears to be a small configuration list scoped by one context code
- `ArrayDataProvider2` will keep transformation logic simple
- after CRUD operations, the page can simply re-fetch and replace the full array

If the team prefers fully service-backed list binding:

- use `vb/ServiceDataProvider`
- bind it to `oj-list-view`
- refresh it after save/delete via `fireDataProviderEvent`

## 10. Action Chains

VBLens MCP guidance recommends JavaScript action chains for implementation work. The following chains are needed.

### 10.1 `LoadCompoundFieldsChain`

Purpose:

- load list data for current context code

Responsibilities:

- set page busy
- call GET service
- normalize service response
- assign list variable/data provider input
- clear page busy
- handle errors

### 10.2 `OpenCreateCompoundFieldDrawerChain`

Purpose:

- open drawer in create mode

Responsibilities:

- initialize empty form state
- set mode to `create`
- open drawer

### 10.3 `OpenEditCompoundFieldDrawerChain`

Purpose:

- open drawer in edit mode for selected row

Responsibilities:

- map selected row into form state
- set mode to `edit`
- open drawer

### 10.4 `CancelCompoundFieldDrawerChain`

Purpose:

- close drawer safely

Responsibilities:

- check for unsaved changes if dirty tracking is enabled
- close drawer or keep it open based on user confirmation

### 10.5 `SaveCompoundFieldChain`

Purpose:

- submit create or update from one action

Responsibilities:

- validate form
- branch by `drawerMode`
- call create or update REST service
- show confirmation notification
- close drawer
- reload list

### 10.6 `DeleteCompoundFieldChain`

Purpose:

- delete selected compound field

Responsibilities:

- confirm delete
- call delete REST service using `fieldId`
- show confirmation notification
- close drawer
- reload list

### 10.7 `FieldCategoryChangedChain`

Purpose:

- update available member fields when category changes

Use only if member field options depend on the selected category.

## 11. Suggested Artifact Set

If this is implemented as a new VBCS page, the minimum artifacts should be:

- `manage-compound-fields-page.json`
- `manage-compound-fields-page.html`
- `LoadCompoundFieldsChain.js`
- `OpenCreateCompoundFieldDrawerChain.js`
- `OpenEditCompoundFieldDrawerChain.js`
- `CancelCompoundFieldDrawerChain.js`
- `SaveCompoundFieldChain.js`
- `DeleteCompoundFieldChain.js`
- optional page module JS if shared mapping helpers are needed

## 12. Validation Rules

Minimum validation rules:

- field name is required
- field category is required
- at least one member field must be selected
- field name should be unique within the current context, if the backend enforces uniqueness

Recommended UX:

- inline component validation on form controls
- drawer-level error message for backend failures
- disable save while request is in flight

## 13. Accessibility Requirements

Based on VBLens MCP component guidance:

- every icon-only `oj-c-button` must still have a descriptive `label`
- `oj-drawer-popup` must have `aria-label` or `aria-labelledby`
- `oj-c-input-text` must have `label-hint` or equivalent accessible label
- `oj-c-select-single` labels must come from translated strings
- all focus should return to the invoking control after drawer close

Additional requirements:

- keyboard access to Add, Edit, Save, Cancel, Delete
- visible busy state during list load and save/delete
- confirmation messaging that is screen-reader friendly

## 14. Translation Strategy

Status:

- **environment verification not performed**
- **no existing translation bundle keys were verified from a repo or runtime environment**

Design guidance from VBLens MCP:

- use translation bundles at flow or page scope
- do not hardcode user-facing strings in the final implementation
- use verified existing keys where available, otherwise add new translation entries through the standard FA translation process

Text that requires translation coverage includes:

- page title
- section title
- Add, Save, Cancel, Delete
- drawer titles for create and edit
- field labels
- validation and notification messages
- empty state text

## 15. Security and Environment Considerations

The provided service examples include basic-auth credentials. Those credentials must **not** be hardcoded in page variables, action chains, source files, or browser-visible code.

Recommended approach:

- configure the REST service connection in the VBCS service catalog or environment
- let runtime credentials come from the configured environment/service connection
- avoid logging request headers or credentials in action chains

## 16. Error Handling

Expected failure cases:

- GET service fails
- create/update/delete service fails
- update contract mismatch
- invalid payload returned from the backend

Recommended behavior:

- show persistent error notification on load failure
- keep drawer open on save failure
- show field-level errors when the backend returns validation details
- do not optimistically mutate the list unless the service contract is fully stable

## 17. Open Questions

These items should be confirmed before implementation starts:

1. What is the exact payload schema for GET, create, update, and delete?
2. Is update really `POST`, or should it be `PATCH` or `PUT`?
3. What field uniquely identifies a compound field in create/update payloads besides `FieldId`?
4. Is there an LOV or describe endpoint for:
   - field category options
   - available member fields by category or context
5. Is `ContextCode` always fixed to `ORA_SUBMISSION`, or should it come from caller/page parameter?
6. Should delete be available only in edit mode, or also as a row-level action on the main list?

## 18. Final Recommendation

Build the page as a Redwood management overview using:

- `oj-sp-general-overview-page` for the shell
- `oj-list-view` for the repeating list
- `oj-c-button` for Add/Edit/Save/Delete actions
- `oj-drawer-popup` for create/edit
- `oj-c-form-layout`, `oj-c-input-text`, `oj-c-select-single`, and `oj-c-checkboxset` for the drawer form

This combination keeps the page aligned with Redwood page patterns, keeps the main UX simple, and isolates create/edit logic without turning the full page into a transaction form.

## 19. MCP Source Notes

The component and concept recommendations in this document are based on VBLens MCP guidance for:

- `oj-sp-general-overview-page`
- `oj-list-view`
- `oj-drawer-popup`
- `oj-c-button`
- `oj-c-form-layout`
- `oj-c-input-text`
- `oj-c-select-single`
- `oj-c-checkboxset`
- VBCS concepts: `ServiceDataProvider`, `ActionChain`, `Variables`, and `Translation`
