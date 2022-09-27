// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2022 VMware, Inc. or its affiliates. All rights reserved.

package com.rabbitmq.jms.client;

import java.util.function.Predicate;

abstract class Utils {

  private Utils() {

  }

  static final Predicate<String> SUBSCRIPTION_NAME_PREDICATE = name -> {
    if (name == null) {
      return true;
    }
    if (name.length() > 128) {
      return false;
    }
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (c != '_' && c != '.' && c != '-' && !Character.isLetter(c) && !Character.isDigit(c)) {
        return false;
      }
    }
    return true;
  };

}
