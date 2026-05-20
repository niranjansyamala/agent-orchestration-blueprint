# Manage Compound Fields Flow Diagram

```mermaid
flowchart TD
    A[User opens Manage Compound Fields page] --> B[vbEnter event fires]
    B --> C[LoadCompoundFieldsChain]
    C --> D[Call GET Compound Fields by ContextCode]
    D --> E{GET success?}
    E -- Yes --> F[Map response to page variables / data provider]
    F --> G[Render Compound Fields list]
    E -- No --> H[Show page load error notification]

    G --> I{User action}

    I --> J[Click Add]
    J --> K[Open drawer in Create mode]
    K --> L[Initialize empty drawer form]
    L --> M[User enters field details]
    M --> N[Click Save]
    N --> O[Validate form]
    O --> P{Form valid?}
    P -- No --> Q[Show validation messages]
    P -- Yes --> R[Call Create Compound Field REST API]
    R --> S{Create success?}
    S -- Yes --> T[Close drawer]
    T --> U[Reload compound fields list]
    U --> V[Show success notification]
    S -- No --> W[Keep drawer open and show save error]

    I --> X[Click Edit icon]
    X --> Y[Open drawer in Edit mode]
    Y --> Z[Populate drawer with selected row data]
    Z --> AA[User updates details]
    AA --> AB[Click Save]
    AB --> AC[Validate form]
    AC --> AD{Form valid?}
    AD -- No --> AE[Show validation messages]
    AD -- Yes --> AF[Call Update Compound Field REST API]
    AF --> AG{Update success?}
    AG -- Yes --> AH[Close drawer]
    AH --> AI[Reload compound fields list]
    AI --> AJ[Show success notification]
    AG -- No --> AK[Keep drawer open and show update error]

    Y --> AL[Click Delete]
    AL --> AM[Show delete confirmation]
    AM --> AN{Confirmed?}
    AN -- No --> Y
    AN -- Yes --> AO[Call Delete Compound Field REST API]
    AO --> AP{Delete success?}
    AP -- Yes --> AQ[Close drawer]
    AQ --> AR[Reload compound fields list]
    AR --> AS[Show delete success notification]
    AP -- No --> AT[Keep drawer open and show delete error]

    K --> AU[Click Cancel]
    Y --> AU
    AU --> AV{Unsaved changes?}
    AV -- Yes --> AW[Show unsaved changes confirmation]
    AW --> AX{Discard changes?}
    AX -- No --> K
    AX -- Yes --> AY[Close drawer]
    AV -- No --> AY
```

## Notes

- `GET` is used to load all compound fields for the supplied `ContextCode`.
- `Create` and `Delete` flows are straightforward from the supplied service contract.
- `Update` is shown as a separate save path, but the exact API contract still needs confirmation because the provided update definition is ambiguous.
