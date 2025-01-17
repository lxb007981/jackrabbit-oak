/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.security.authorization.permission;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import org.apache.jackrabbit.guava.common.base.Strings;
import org.apache.jackrabbit.oak.api.PropertyState;
import org.apache.jackrabbit.oak.api.Root;
import org.apache.jackrabbit.oak.api.Tree;
import org.apache.jackrabbit.oak.api.Type;
import org.apache.jackrabbit.oak.plugins.tree.ReadOnly;
import org.apache.jackrabbit.oak.spi.security.ConfigurationParameters;
import org.apache.jackrabbit.oak.spi.security.authorization.permission.PermissionConstants;
import org.apache.jackrabbit.oak.spi.security.principal.AdminPrincipal;
import org.apache.jackrabbit.oak.spi.security.principal.SystemPrincipal;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.apache.jackrabbit.util.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility methods to evaluate permissions.
 */
public final class PermissionUtil implements PermissionConstants {

    private PermissionUtil() {}

    @Nullable
    public static String getParentPathOrNull(@NotNull final String path) {
        if (path.length() <= 1) {
            return null;
        } else {
            int idx = path.lastIndexOf('/');
            if (idx == 0) {
                return "/";
            } else {
                return path.substring(0, idx);
            }
        }
    }

    @NotNull
    public static String getEntryName(@Nullable String accessControlledPath) {
        String path = Strings.nullToEmpty(accessControlledPath);
        return String.valueOf(path.hashCode());
    }

    public static boolean checkACLPath(@NotNull NodeBuilder node, @NotNull String path) {
        PropertyState property = node.getProperty(REP_ACCESS_CONTROLLED_PATH);
        return property != null && path.equals(property.getValue(Type.STRING));
    }

    public static boolean checkACLPath(@NotNull Tree node, @NotNull String path) {
        PropertyState property = node.getProperty(REP_ACCESS_CONTROLLED_PATH);
        return property != null && path.equals(property.getValue(Type.STRING));
    }

    @NotNull
    public static Tree getPermissionsRoot(@NotNull Root root, @NotNull String permissionRootName) {
        return root.getTree(PERMISSIONS_STORE_PATH + '/' + permissionRootName);
    }

    @NotNull
    public static Tree getPrincipalRoot(@NotNull Tree permissionsTree, @NotNull String principalName) {
        return permissionsTree.getChild(Text.escapeIllegalJcrChars(principalName));
    }

    public static boolean isAdminOrSystem(@NotNull Set<Principal> principals, @NotNull ConfigurationParameters config) {
        if (principals.contains(SystemPrincipal.INSTANCE)) {
            return true;
        } else {
            Set<String> adminNames = config.getConfigValue(PARAM_ADMINISTRATIVE_PRINCIPALS, Collections.EMPTY_SET);
            for (Principal principal : principals) {
                if (principal instanceof AdminPrincipal || adminNames.contains(principal.getName())) {
                    return true;
                }
            }
            return false;
        }
    }

    @Nullable
    public static String getPath(@Nullable Tree parentBefore, @Nullable Tree parentAfter) {
        String path = null;
        if (parentBefore != null) {
            path = parentBefore.getPath();
        } else if (parentAfter != null) {
            path = parentAfter.getPath();
        }
        return path;
    }

    @Nullable
    public static Tree getReadOnlyTreeOrNull(@Nullable Tree tree, @NotNull Root readOnlyRoot) {
        if (tree instanceof ReadOnly) {
            return tree;
        } else {
            return (tree == null) ? null : readOnlyRoot.getTree(tree.getPath());
        }
    }

    @NotNull
    public static Tree getReadOnlyTree(@NotNull Tree tree, @NotNull Root readOnlyRoot) {
        if (tree instanceof ReadOnly) {
            return tree;
        } else {
            return readOnlyRoot.getTree(tree.getPath());
        }
    }
}
