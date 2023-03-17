/*
 * Copyright 2014-2023 TNG Technology Consulting GmbH
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
 */
package com.tngtech.archunit.library.modules.syntax;

import java.lang.annotation.Annotation;

import com.tngtech.archunit.PublicAPI;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.modules.AnnotationDescriptor;
import com.tngtech.archunit.library.modules.ArchModule;

import static com.tngtech.archunit.PublicAPI.Usage.ACCESS;

@PublicAPI(usage = ACCESS)
public interface ModulesByAnnotationShould<ANNOTATION extends Annotation> extends ModulesShould<AnnotationDescriptor<ANNOTATION>> {

    /**
     * Like {@link #respectTheirAllowedDependencies(DescribedPredicate, ModuleDependencyScope)}, but the allowed dependencies will be automatically
     * derived from the {@link ANNOTATION} property named {@code annotationPropertyName}. This property *must* be of type {@code String[]}
     * and contain the {@link ArchModule#getName() names} of the {@link ArchModule}s to which access is allowed.
     * <br><br>
     * For example, given the user-defined annotation
     * <pre><code>
     * &#64;interface MyModule {
     *   String name();
     *
     *   String[] allowedDependencies() default {};
     * }
     * </code></pre>
     * and the annotated root classes
     * <pre><code>
     * &#64;MyModule(name = "Module One", allowedDependencies = {"Module Two", "Module Three"})
     * interface ModuleOneDescriptor {}
     *
     * &#64;MyModule(name = "Module Two", allowedDependencies = {"Module Three"})
     * interface ModuleTwoDescriptor {}
     *
     * &#64;MyModule(name = "Module Three")
     * interface ModuleThreeDescriptor {}
     * </code></pre>
     * Then the allowed dependencies between the modules would be
     * <pre><code>
     * ,----------.
     * |Module One| ------------.
     * `----------'              \
     *      |                    |
     *      |                    |
     *      v                    v
     * ,----------.         ,------------.
     * |Module Two| ------> |Module Three|
     * `----------'         `------------'
     * </code></pre>
     *
     * @param annotationPropertyName The name of the property declared within {@link ANNOTATION} that declares allowed dependencies to other {@link ArchModule}s by name
     * @param dependencyScope        Allows to adjust which {@link Dependency dependencies} are considered relevant by the rule
     * @return An {@link ArchRule} to be checked against a set of {@link JavaClasses}
     */
    @PublicAPI(usage = ACCESS)
    ModulesRule<AnnotationDescriptor<ANNOTATION>> respectTheirAllowedDependenciesDeclaredIn(String annotationPropertyName, ModuleDependencyScope dependencyScope);
}
