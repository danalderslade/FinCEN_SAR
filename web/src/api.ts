import type { BatchSummary } from './types'

export async function fetchBatches(): Promise<BatchSummary[]> {
  const response = await fetch('/api/v1/batches', {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error(`Batch request failed with status ${response.status}`)
  }

  return (await response.json()) as BatchSummary[]
}