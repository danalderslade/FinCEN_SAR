# FinCEN SAR Platform
## End-User Guide

This guide is written for analysts, reviewers, approvers, and administrators who use the web application day to day.

---

## Who This Guide Is For

Use this guide if you need to:

- Sign in and navigate the platform
- Create and manage SAR filing batches
- Add and complete SAR activities
- Move filings through review and submission statuses
- Resolve common user-facing issues

If you are looking for developer setup, API details, or deployment steps, use [README.md](../README.md).

---

## Quick Start (2 Minutes)

1. Open the web app: `http://localhost:3000`
2. Sign in with your assigned account
3. Open **Batches** from the left menu
4. Select **+ New Batch**
5. Add at least one activity and complete the wizard
6. Use workflow actions to move the batch forward

Default local training account:

- Username: `admin`
- Password: `Admin123!`

---

## Application Tour

### Left Sidebar

- **Dashboard**: system snapshot and recent work
- **Batches**: create, search, and open filing batches
- **Sign Out**: end your session safely

### Main Workspace

- Header: current page and context
- Action buttons: create, delete, workflow actions
- Data tables: sortable/paginated records
- Status badges: current filing state

### Filing Statuses

- `DRAFT`: actively being prepared
- `REVIEW`: ready for reviewer/approver check
- `SUBMITTED`: sent for filing workflow completion
- `ACKNOWLEDGED`: accepted/finalized
- `REJECTED`: returned due to issues

---

## Core Workflow

### 1. Create a Batch

1. Go to **Batches**
2. Select **+ New Batch**
3. Enter:
   - **Activity Count**
   - **Party Count**
   - **Total Amount** (optional)
4. Select **Create Batch**

Expected result:

- You are redirected to the new batch detail page
- Batch starts in `DRAFT`

### 2. Add Activities to the Batch

1. Open the batch detail page
2. Select **Add Activity**
3. Open the activity wizard

Each activity is a single SAR report record.

### 3. Complete the Activity Wizard

The wizard has 9 sections. You can move between sections as needed.

1. **Activity Header**
2. **Filing Type**
3. **Support Document**
4. **Parties**
5. **Suspicious Activity**
6. **IP Addresses**
7. **Cyber Events**
8. **Assets**
9. **Narratives**

Good practice:

- Save complete, accurate values per section
- Use realistic dates and required party details
- Keep narratives clear, factual, and chronological

### 4. Submit Through Filing Workflow

From the batch detail page, use workflow buttons according to your role:

- **Submit for Review**: `DRAFT` to `REVIEW`
- **Return to Draft**: `REVIEW` to `DRAFT`
- **Submit to FinCEN**: `REVIEW` to `SUBMITTED`
- **Acknowledge**: `SUBMITTED` to `ACKNOWLEDGED`
- **Reject**: `SUBMITTED` to `REJECTED`

---

## Role-Based Access (What You Can Do)

| Capability | Analyst | Reviewer | Approver | Admin |
|---|---|---|---|---|
| Create/edit batches | Yes | Yes | Yes | Yes |
| Create/edit activities | Yes | Yes | Yes | Yes |
| Delete records | Yes | Yes | Yes | Yes |
| Submit for review | No | Yes | Yes | Yes |
| Return to draft | No | Yes | Yes | Yes |
| Submit to FinCEN | No | No | Yes | Yes |
| Acknowledge or reject | No | No | Yes | Yes |
| Manage users | No | No | No | Yes |

If a button is missing or disabled, your account role likely does not allow that action.

---

## Data Entry Guidance

### Required vs Optional

- Required fields must be completed before successful workflow progression
- Optional fields improve filing quality but may not block saving

### Dates

- Use real event and filing dates
- Avoid future dates for filing/event records unless explicitly intended

### Narrative Quality

A strong narrative includes:

- Who was involved
- What happened
- When it happened
- Why it is suspicious
- Dollar values and transaction patterns

### Party Records

Ensure party type is correct before entering details. Different party types require different supporting data.

---

## Tips for Faster Work

- Use batch filters to focus on one status at a time
- Open recent batches from Dashboard for quick continuation
- Complete one activity fully before starting the next
- Re-check `REVIEW` items for missing or inconsistent details

---

## Common Tasks

### Open an Existing Batch

1. Go to **Batches**
2. Filter or page to locate the record
3. Select the batch ID link

### Delete a Batch

1. Locate the batch in **Batches**
2. Select **Delete**
3. Confirm the prompt

Warning: deleting a batch also deletes its activities and nested records.

### Download XML (If Available)

On batch detail, select the XML download action when the filing is ready.

---

## Troubleshooting

### "502 Bad Gateway" in Browser

What it means:

- The web server is up, but API was temporarily unreachable

What to do:

1. Wait 20-40 seconds and refresh
2. Verify local services are running
3. Sign in again if your session expired

### "403" or "Unauthorized"

What it means:

- You are not authenticated or your role cannot perform the action

What to do:

1. Sign out and sign in again
2. Verify you are using the correct role
3. Retry action from the intended status (for workflow transitions)

### "Create Batch" appears to do nothing

What to check:

1. Confirm required numeric fields are valid
2. Look for error banner near the form
3. Re-authenticate if session is stale

### Session Keeps Expiring

- This is usually expected token expiration behavior
- Sign in again to continue

---

## FAQ

### Can I edit submitted filings?

Typically no. Move back to `DRAFT` first (if your role allows), then edit.

### Why are workflow buttons missing?

Buttons are status-based and role-based. Both conditions must allow the action.

### How many narratives can I add?

Up to 5 narrative blocks per activity.

---

## Best Practices Checklist

Before moving a batch to `REVIEW`:

- All required activity fields complete
- Parties and suspicious activity sections reviewed
- Narrative is clear and complete
- Dates and amounts are internally consistent

Before `SUBMITTED`:

- Reviewer comments resolved
- No validation warnings remain
- Batch contents are final

---

## Keyboard and Accessibility Notes

- Use `Tab` and `Shift+Tab` to move through fields
- Use browser zoom for readability as needed
- Use clear contrast mode/settings in your browser or OS for visual comfort

---

## Need Help

If you are blocked:

1. Capture the page, action attempted, and exact error text
2. Share your user role and batch ID
3. Contact your system administrator or support team

---

## Document Information

- Product: FinCEN SAR Filing Platform
- Audience: End users (Analyst, Reviewer, Approver, Admin)
- Version date: March 16, 2026
