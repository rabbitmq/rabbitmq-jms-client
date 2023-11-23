// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

package com.rabbitmq.jms.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class UtilsTest {

  @CsvSource({
      "valid-subscription-name,true",
      "valid_subscription_name,true",
      "valid.subscription.name,true",
      "yet-another_valid.subscription.name,true",
      "invalid?subscription!name,false",
      "invalid(subscription)name,false",
      "invalid(subscription)name,false",
      "very.loooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
          + "ooooooooooooooooooooooooooooooooooooooooooooong.subscription.name,true",
      "too.loooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
          + "oooooooooooooooooooooooooooooooooooooooooooooooooooong.subscription.name,false",
  })
  @ParameterizedTest
  void subscriptionNameValidation(String subscriptionName, boolean valid) {
    // https://jakarta.ee/specifications/messaging/3.1/jakarta-messaging-spec-3.1.html#subscription-name-characters-and-length
    assertThat(Utils.SUBSCRIPTION_NAME_PREDICATE.test(subscriptionName)).isEqualTo(valid);
  }
}
