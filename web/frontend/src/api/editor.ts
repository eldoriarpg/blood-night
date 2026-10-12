/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import axios, {type AxiosInstance} from 'axios'
import type {Catalog, ChangeAccepted, RevisionInfo, SessionView} from '~/api/generated/schema'
import type {EditorPayload} from '~/api/generated/payload'
import type {PatchOperation} from '~/util/patch/jsonPatch'

/** The editor service's calls for one participant of one session; every call carries their token. */
export interface EditorApi {
    session(): Promise<SessionView>
    catalog(version: string): Promise<Catalog>
    change(operations: readonly PatchOperation[]): Promise<ChangeAccepted>
    replace(payload: EditorPayload): Promise<ChangeAccepted>
    revisions(): Promise<RevisionInfo[]>
    revision(number: number): Promise<EditorPayload>
    restore(number: number): Promise<ChangeAccepted>
    submit(): Promise<void>
    take(section: string): Promise<void>
    release(section: string): Promise<void>
}

/**
 * The calls for a participant of a session.
 *
 * @param id the session's id
 * @param token the participant's token, from their personal link
 * @param http the HTTP client, for tests
 * @returns the calls
 */
export function editorApi(id: string, token: string, http: AxiosInstance = editorHttp(token)): EditorApi {
    const session = `/sessions/${encodeURIComponent(id)}`
    return {
        session: () => http.get<SessionView>(session).then(response => response.data),
        catalog: version => http.get<Catalog>(`/catalogs/${encodeURIComponent(version)}`).then(response => response.data),
        change: operations => http.post<ChangeAccepted>(`${session}/changes`, {operations}).then(response => response.data),
        replace: payload => http.put<ChangeAccepted>(`${session}/payload`, payload).then(response => response.data),
        revisions: () => http.get<RevisionInfo[]>(`${session}/revisions`).then(response => response.data),
        revision: number => http.get<EditorPayload>(`${session}/revisions/${number}`).then(response => response.data),
        restore: number => http.post<ChangeAccepted>(`${session}/revisions/${number}/restore`).then(response => response.data),
        submit: () => http.post(`${session}/submit`).then(() => undefined),
        take: section => http.post(`${session}/locks`, {section}).then(() => undefined),
        release: section => http.delete(`${session}/locks`, {data: {section}}).then(() => undefined),
    }
}

function editorHttp(token: string): AxiosInstance {
    return axios.create({
        baseURL: '/api/v1/editor',
        timeout: 30000,
        headers: {Authorization: `Bearer ${token}`, Accept: 'application/json'},
    })
}

/**
 * @param error what a call failed with
 * @returns the HTTP status and the service's message, when the service answered
 */
export function failure(error: unknown): {status: number, message: string, details: Record<string, string>} {
    if (axios.isAxiosError(error) && error.response) {
        const body = error.response.data as {title?: string, details?: Record<string, string>} | undefined
        return {status: error.response.status, message: body?.title ?? error.message, details: body?.details ?? {}}
    }
    return {status: 0, message: (error as Error)?.message ?? String(error), details: {}}
}
