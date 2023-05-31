/*
 *    Copyright 2010-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.jpetstore.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.jpetstore.core.EventStore;
import org.mybatis.jpetstore.core.event.DomainEvent;
import org.mybatis.jpetstore.domain.Account;

public class EventSourcedAccountRepository {
  private EventStore eventStore;
  private Map<String, String> accountIdCache;

  public EventSourcedAccountRepository(EventStore eventStore) {
    this.eventStore = eventStore;
    this.accountIdCache = new HashMap<>();
  }

  public String save(Account account) {
    String streamId = null;
    for (DomainEvent e : account.getEvents()) {
      try {
        streamId = e.getStreamId();
        eventStore.appendToStream(streamId, e);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    if (streamId != null) {
      accountIdCache.put(account.getUsername(), account.getAccountId());
    }
    return streamId;
  }

  public Account findByUsernameAndPassword(String username, String password) {
    String accountId = accountIdCache.get(username);
    if (accountId == null) {
      return null;
    }
    String streamId = Account.class.getName() + "." + accountId;
    List<DomainEvent> events = eventStore.getStream(streamId);
    Account account = new Account(accountId);
    for (DomainEvent event : events) {
      account.mutate(event);
    }
    if (account.getPassword().equals(password)) {
      return account;
    }
    return null;
  }

  public Account findBy(String accountId) {
    String streamId = Account.class.getName() + "." + accountId;
    List<DomainEvent> events = eventStore.getStream(streamId);
    Account account = new Account(accountId);
    for (DomainEvent event : events) {
      account.mutate(event);
    }
    return account;
  }

  public Account findBy(String accountId, long version) {
    String streamId = Account.class.getName() + "." + accountId;
    List<DomainEvent> events = eventStore.getStream(streamId, version);
    Account account = new Account(accountId);
    for (DomainEvent event : events) {
      account.mutate(event);
    }
    return account;
  }
}
