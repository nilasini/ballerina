/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.completions.resolvers;

import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.tree.BLangResource;

import java.util.ArrayList;

/**
 * Item Resolver for the connector action context.
 */
public class ConnectorActionContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(TextDocumentServiceContext completionContext) {
        // Same item resolving logic is used for both the BLangResource and the BLangAction
        return CompletionItemResolver.getResolverByClass(BLangResource.class).resolveItems(completionContext);
    }
}
