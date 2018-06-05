/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.parquet.crypto;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import org.apache.parquet.bytes.BytesUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class VaultKeyRetriever implements DecryptionKeyRetriever{

    final private String token;
    final private Map<Integer, byte[]> keys;
	final Vault vault;

    public VaultKeyRetriever(String token, String vaultAddress) throws IOException{
        this.token = token;
        keys = new HashMap<Integer, byte[]>();
		VaultConfig config;
		try {
			config = new VaultConfig()
					.address(vaultAddress)
					.token(token)
					.build();
		}catch(Exception e){
			throw new IOException("Unable to reach Vault server.");
		}
		vault = new Vault(config);
    }
    private byte[] getKeyFromVault(Integer keyId){
        try {
            String key = vault.logical().read("secret/keys").getData().get(keyId.toString());
            //System.out.println("Log: retrieving key " + keyId + ".");
            return Base64.getDecoder().decode(key);
        } catch (Exception e){
        	System.out.println("Could not retrieve key from Vault. (No permission or doesn't exist)");
            return null;
        }
    }
    public byte[] getKeyFromInt(Integer keyId){
        byte[] cachedKey = keys.get(keyId);
        if(cachedKey == null)
            return getKeyFromVault(keyId);
        return cachedKey;
    }
    @Override public byte[] getKey(byte[] keyMetaData){
        Integer keyId = BytesUtils.bytesToInt(keyMetaData);
        return getKeyFromInt(keyId);
    }
}
