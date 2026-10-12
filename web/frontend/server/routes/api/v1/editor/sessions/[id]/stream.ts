/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {defineWebSocketHandler} from 'h3'
import {relaySocket} from '../../../../../../utils/relaySocket'

/**
 * The live stream of an editor session, relayed to the backend's stream of the same session.
 *
 * <p>Only the upgrade reaches this route. A plain HTTP request to the address is forwarded by the
 * backend proxy middleware before any route runs.
 */
export default defineWebSocketHandler(relaySocket())
