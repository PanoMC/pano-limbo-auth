/*
 * Copyright (C) 2021 - 2025 Elytrium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.elytrium.limboauth.api;

/**
 * Lets an external system - for example a website's account database - act as the authority for
 * player passwords, replacing LimboAuth's built-in local BCrypt verification.
 *
 * <p>A provider is installed through
 * {@link net.elytrium.limboauth.LimboAuth#setExternalPasswordProvider(ExternalPasswordProvider)}.
 * Once installed, {@link #verifyPassword(String, String)} is consulted at the very top of every
 * login attempt, before LimboAuth ever looks at its own stored hash. This mirrors the way AuthMe
 * lets a custom {@code EncryptionMethod} take over password checking: the external system, and not
 * the plugin's own database, decides whether a supplied password is valid.
 *
 * <p>Returning {@code null} from {@link #verifyPassword(String, String)} opts a given player out, so
 * LimboAuth transparently falls back to its own local BCrypt hash for that player. This makes it
 * possible to mix externally-managed and locally-managed accounts in the same database.
 *
 * <p>This interface deliberately refers only to standard JDK types, so that a consumer implementing
 * it does not have to depend on any other (AGPL-licensed) LimboAuth type.
 */
public interface ExternalPasswordProvider {

  /**
   * Verifies the supplied clear-text password for the given player.
   *
   * <p>Returning {@code null} signals that this provider does not manage the given player; LimboAuth
   * then falls back to verifying the password against its own local BCrypt hash. Returning a
   * non-null value takes full ownership of the decision and skips that fallback entirely.
   *
   * @param lowercaseNickname the player's nickname, already lower-cased with {@link java.util.Locale#ROOT}
   * @param password the clear-text password the player supplied
   * @return {@link Boolean#TRUE} to accept the login, {@link Boolean#FALSE} to reject it, or
   *     {@code null} to fall back to LimboAuth's own local BCrypt verification
   */
  Boolean verifyPassword(String lowercaseNickname, String password);

  /**
   * Notification hook describing a registration performed directly through LimboAuth.
   *
   * <p>The default implementation does nothing; an external provider may override it to mirror the
   * newly created account into its own store.
   *
   * @param lowercaseNickname the player's nickname, already lower-cased with {@link java.util.Locale#ROOT}
   * @param password the clear-text password chosen at registration
   * @param ip the player's IP address at registration time
   */
  default void onRegister(String lowercaseNickname, String password, String ip) {
  }

  /**
   * Notification hook describing a password change performed directly through LimboAuth.
   *
   * <p>The default implementation does nothing; an external provider may override it to mirror the
   * new password into its own store.
   *
   * @param lowercaseNickname the player's nickname, already lower-cased with {@link java.util.Locale#ROOT}
   * @param newPassword the new clear-text password
   */
  default void onPasswordChanged(String lowercaseNickname, String newPassword) {
  }

  /**
   * Notification hook describing an unregistration performed directly through LimboAuth.
   *
   * <p>The default implementation does nothing; an external provider may override it to remove the
   * matching account from its own store.
   *
   * @param lowercaseNickname the player's nickname, already lower-cased with {@link java.util.Locale#ROOT}
   */
  default void onUnregister(String lowercaseNickname) {
  }
}
