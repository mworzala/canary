/**
 * Canary Packet Recording (.cpr)
 * <p>CPR is a basic binary format for storing a set of packets sent by a player. </p>
 * <p>The packets are stored in wire format, meaning they are server platform agnostic.</p>
 *
 * <p>
 * <p>Some metadata is required when reading or writing the files:</p>
 * <p>Magic Number      0x41FF  | If the file does not start with this constant, it is not considered a valid CPR file.</p>
 * <p>Format Version    1       | The current CPR version. This is changed when the file format changes.</p>
 * <p>Protocol Version          | The Minecraft protocol version at which the file was written.</p>
 *
 * <p>
 * <p>The format simply contains a header and entry for each individual packet.</p>
 *
 * <p>
 * <p>The header is a total of 64 bytes (as of the current version) in the following format:</p>1
 * <p>2 bytes | magic number</p>
 * <p>2 bytes | CPR version</p>
 * <p>4 bytes | minecraft protocol version</p>
 * <p>2 bytes | # of packets in recording (N)</p>
 * <p>8 bytes | start position x</p>
 * <p>8 bytes | start position y</p>
 * <p>8 bytes | start position z</p>
 * <p>4 bytes | start position pitch</p>
 * <p>4 bytes | start position yaw</p>
 * <p>22 bytes | unused</p>
 *
 * <p>
 * <p>The remaining space in the file is taken up by N entries of the following format:</p>
 * <p>4 bytes | time delta (# of milliseconds after recording start)</p>
 * <p>2 bytes | packet id</p>
 * <p>N bytes | raw packet data</p>
 *
 * <p>
 * <p>Other Notes:</p>
 * <p>The CPR format guarantees that the packets are in sequential order.</p>
 */
package com.mattworzala.canary.server.recording.cpr;