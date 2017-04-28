/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.analysisteetime.signature;

import kieker.analysisteetime.model.analysismodel.type.ComponentType;

/**
 * A {@link ComponentNameBuilder} can be used to create the component name from a package name and a component name.
 *
 * @author S�ren Henning
 *
 * @since 1.13
 */
public interface ComponentNameBuilder {

	public String build(final String packageName, final String name);

	public default String build(final ComponentType componentType) {
		return this.build(componentType.getPackage(), componentType.getName());
	}

}
