import { createClient } from '@supabase/supabase-js'

export const SUPABASE_URL = 'https://mcegdtcufzxpmezpncab.supabase.co'
export const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1jZWdkdGN1Znp4cG1lenBuY2FiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAwOTIzNzQsImV4cCI6MjEwNTY2ODM3NH0.cRUAUjDvVEj2vc-PVk3_bICkrDYiMwiLPJBBGm1UQZU'

export const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
  auth: {
    persistSession: true,
    autoRefreshToken: true,
    detectSessionInUrl: true,
    storageKey: 'nintec_admin_auth'
  }
})
